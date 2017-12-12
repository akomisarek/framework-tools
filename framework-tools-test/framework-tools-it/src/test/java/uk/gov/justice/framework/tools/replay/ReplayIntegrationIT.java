package uk.gov.justice.framework.tools.replay;


import static java.util.UUID.randomUUID;
import static org.junit.Assert.assertTrue;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelope;

import uk.gov.justice.services.eventsourcing.repository.jdbc.eventlog.EventLog;
import uk.gov.justice.services.eventsourcing.repository.jdbc.exception.InvalidSequenceIdException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.InputStreamReader;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ReplayIntegrationIT {

    private static final String SWARM_DEBUG_MODE = "-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005";

    private static final TestProperties TEST_PROPERTIES = new TestProperties("test.properties");
    private static final String H2_DRIVER = "org.h2.Driver";

    private static final UUID STREAM_ID = randomUUID();

    private static TestEventLogRepository EVENT_LOG_REPOSITORY;


    @Before
    public void setUpDB() throws Exception {
        EVENT_LOG_REPOSITORY = new TestEventLogRepository(initEventStoreDb());
    }

    @Test
    public void runReplayTool() throws Exception {
        final String command = createCommandToExecuteReplay();

        final UUID streamId = randomUUID();
        insertEventLogData(streamId);
        final boolean matches = runCommand(command);
        assertTrue(matches);
    }

    @After
    public void tearDown() throws SQLException {
        final PreparedStatement preparedStatement = EVENT_LOG_REPOSITORY.getDataSource().getConnection().prepareStatement("delete from event_log");
        preparedStatement.executeUpdate();
        EVENT_LOG_REPOSITORY.getDataSource().getConnection().close();
    }

    private static DataSource initEventStoreDb() throws Exception {
        return initDatabase("db.eventstore.url", "db.eventstore.userName",
                "db.eventstore.password", "liquibase/event-store-db-changelog.xml", "liquibase/snapshot-store-db-changelog.xml", "liquibase/event-buffer-changelog.xml");
    }

    private EventLog eventLogFrom(final String eventName) {

        final JsonEnvelope jsonEnvelope = envelope()
                .with(metadataWithRandomUUID(eventName)
                        .createdAt(ZonedDateTime.now())
                        .withStreamId(STREAM_ID).withVersion(1L))
                .withPayloadOf("test", "a string")
                .build();

        final Metadata metadata = jsonEnvelope.metadata();
        final UUID id = metadata.id();

        final UUID streamId = metadata.streamId().get();
        final Long sequenceId = 1L;
        final String name = metadata.name();
        final String payload = jsonEnvelope.payloadAsJsonObject().toString();
        final ZonedDateTime createdAt = metadata.createdAt().get();

        return new EventLog(id, streamId, sequenceId, name, metadata.asJsonObject().toString(), payload, createdAt);
    }


    private String createCommandToExecuteReplay() {

        final String replayJarLocation = getResource("framework-tools-replay*.jar");
        final String standaloneDSLocation = getResource("standalone-ds.xml");
        final String listenerLocation = getResource("framework-tools-test-listener*.war");

        String deubug = "";

        if (TEST_PROPERTIES.value("swarm.debug").equals("true")) {
            deubug = SWARM_DEBUG_MODE;
        }

        final String command = commandFrom(deubug, replayJarLocation, standaloneDSLocation, listenerLocation);

        return command;
    }

    private String commandFrom(final String debugString,
                               final String replayJarLocation,
                               final String standaloneDSLocation,
                               final String listenerLocation) {
        return String.format("java -jar %s %s replay -c %s -l %s",
                debugString,
                replayJarLocation,
                standaloneDSLocation,
                listenerLocation);
    }

    private static DataSource initDatabase(final String dbUrlPropertyName,
                                           final String dbUserNamePropertyName,
                                           final String dbPasswordPropertyName,
                                           final String... liquibaseChangeLogXmls) throws Exception {
        final BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(H2_DRIVER);

        dataSource.setUrl(TEST_PROPERTIES.value(dbUrlPropertyName));
        dataSource.setUsername(TEST_PROPERTIES.value(dbUserNamePropertyName));
        dataSource.setPassword(TEST_PROPERTIES.value(dbPasswordPropertyName));
        boolean dropped = false;
        final JdbcConnection jdbcConnection = new JdbcConnection(dataSource.getConnection());

        for (String liquibaseChangeLogXml : liquibaseChangeLogXmls) {
            Liquibase liquibase = new Liquibase(liquibaseChangeLogXml,
                    new ClassLoaderResourceAccessor(), jdbcConnection);
            if (!dropped) {
                liquibase.dropAll();
                dropped = true;
            }
            liquibase.update("");
        }
        return dataSource;
    }

    private String getResource(final String pattern) {
        final File dir = new File(this.getClass().getClassLoader().getResource("").getPath());
        final FileFilter fileFilter = new WildcardFileFilter(pattern);
        return dir.listFiles(fileFilter)[0].getAbsolutePath();
    }

    public boolean runCommand(final String command) throws Exception {

        final Process exec = Runtime.getRuntime().exec(command);
        final BufferedReader reader =
                new BufferedReader(new InputStreamReader(exec.getInputStream()));

        boolean matches = false;
        String line = "";
        while ((line = reader.readLine()) != null) {
            Pattern p = Pattern.compile(".*caught a fish.*", Pattern.MULTILINE | Pattern.DOTALL);
            if (p.matcher(line).matches()) {
                matches = true;
            }
            System.out.println(line);
        }
        return matches;
    }

    private void insertEventLogData(final UUID streamId) throws SQLException, InvalidSequenceIdException {
        EVENT_LOG_REPOSITORY.insert(eventLogFrom("framework.example-test"));
    }
}
