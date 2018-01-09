package uk.gov.justice.framework.tools.replay;


import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.io.*;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static org.junit.Assert.assertTrue;
import static uk.gov.justice.framework.tools.replay.DatabaseUtils.*;

public class ReplayIntegrationIT {

    private static final TestProperties TEST_PROPERTIES = new TestProperties("test.properties");

    private static final long EXECUTION_TIMEOUT = 60L;

    private static TestEventLogRepository EVENT_LOG_REPOSITORY;

    private static DataSource viewStoreDataSource;

    @Before
    public void setUpDB() throws Exception {
        EVENT_LOG_REPOSITORY = new TestEventLogRepository();
        viewStoreDataSource = initViewStoreDb();
    }

    @Test
    public void runReplayTool() throws Exception {
        List<String> insertedEvents = EVENT_LOG_REPOSITORY.insertEventLogData(randomUUID());
        runCommand(createCommandToExecuteReplay());
        assertTrue(viewStoreEvents(viewStoreDataSource).containsAll(insertedEvents));
    }

    @After
    public void tearDown() throws SQLException {
        cleanupDataSource(EVENT_LOG_REPOSITORY.getDataSource(), "event_log");
        cleanupDataSource(viewStoreDataSource, "test");
    }

    private void runCommand(final String command) throws Exception {

        final Process exec = Runtime.getRuntime().exec(command);

        new Thread(() -> {
            System.out.println("Redirecting output...");
            try (final BufferedReader reader =
                         new BufferedReader(new InputStreamReader(exec.getInputStream()))) {

                final Pattern p = Pattern.compile(".*========== ALL TASKS HAVE BEEN DISPATCHED -- SHUTDOWN =================.*", Pattern.MULTILINE | Pattern.DOTALL);
                String line;
                while ((line = reader.readLine()) != null) {

                    System.out.println(line);

                    if (p.matcher(line).matches()) {
                        // Fraction has run so kill server now
                        exec.destroyForcibly();
                        break;
                    }

                }
            }
            catch (IOException ioEx) {
                System.out.println("IOException occurred reading process input stream");
            }

        }).start();

        System.out.println("Process started, waiting for completion..");

        // Give the process 60 seconds to complete and then kill it. Successful test will be
        // determined by querying the ViewStore for associated records later. The above Thread should
        // kill the process inside 60 seconds but wait here and handle shutdown if things take
        // too long for some reason
        boolean processTerminated = exec.waitFor(EXECUTION_TIMEOUT, TimeUnit.SECONDS);

        if (!processTerminated) {
            System.err.println("WildFly Swarm process failed to terminate after 60 seconds!");
            Process terminating = exec.destroyForcibly();

            processTerminated = terminating.waitFor(10L, TimeUnit.SECONDS);
            if (!processTerminated) {
                System.err.println("Failed to forcibly terminate WildFly Swarm process!");
            }
            else {
                System.err.println("WildFly Swarm process forcibly terminated.");
            }
        }
        else {
            System.out.println("WildFly Swarm process terminated by Test.");
        }

    }

    private String createCommandToExecuteReplay() {

        final String replayJarLocation = getResource("framework-tools-replay*.jar");
        final String standaloneDSLocation = getResource("standalone-ds.xml");
        final String listenerLocation = getResource("replay-tool-it-example-listener*.war");

        String debug = "";

        if (TEST_PROPERTIES.value("swarm.debug.enabled").equals("true")) {
            debug = TEST_PROPERTIES.value("swarm.debug.args");
        }

        return commandFrom(debug, replayJarLocation, standaloneDSLocation, listenerLocation);
    }

    private String commandFrom(final String debugString,
                               final String replayJarLocation,
                               final String standaloneDSLocation,
                               final String listenerLocation) {
        return format("java %s -Devent.listener.war=%s -jar %s -c %s",
                debugString,
                listenerLocation,
                replayJarLocation,
                standaloneDSLocation);
    }

    private String getResource(final String pattern) {
        final File dir = new File(this.getClass().getClassLoader().getResource("").getPath());
        final FileFilter fileFilter = new WildcardFileFilter(pattern);
        return dir.listFiles(fileFilter)[0].getAbsolutePath();
    }
}
