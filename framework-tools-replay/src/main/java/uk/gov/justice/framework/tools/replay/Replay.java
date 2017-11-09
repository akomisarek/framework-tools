package uk.gov.justice.framework.tools.replay;

import static org.jboss.shrinkwrap.api.ShrinkWrap.create;
import static org.jboss.shrinkwrap.api.ShrinkWrap.createFromZipFile;
import static org.wildfly.swarm.Swarm.artifact;

import uk.gov.justice.framework.tools.common.command.ShellCommand;

import java.nio.file.Path;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wildfly.swarm.Swarm;
import org.wildfly.swarm.undertow.WARArchive;


@Parameters(separators = "=", commandDescription = "Replay Event Stream Command")
public class Replay implements ShellCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(Replay.class);

    @Parameter(names = "-l", description = "external library")
    private Path library;

    public void run(final String[] args) {

        try {
            new Swarm(args)
                    .start()
                    .deploy(buildDeploymentArtifact())
                    .stop();
            System.exit(0);
        } catch (Exception e) {
            LOGGER.error("Failed to start Wildfly Swarm and deploy War file", e);
        }
    }

    private WARArchive buildDeploymentArtifact() throws Exception {
        final WebArchive webArchive = createFromZipFile(WebArchive.class, library.toFile());

        final FrameworkLibraries frameworkLibraries = new FrameworkLibraries(
                "uk.gov.justice.services:event-repository-jdbc",
                "uk.gov.justice.services:framework-api-core",
                "uk.gov.justice.services:core",
                "uk.gov.justice.services:persistence-jdbc",
                "uk.gov.justice.services:event-buffer-core");

        final WebArchive excludeGeneratedApiClasses = create(WebArchive.class, "ExcludeGeneratedApiClasses")
                .merge(webArchive, frameworkLibraries.exclusionFilter());

        try {
            return create(WARArchive.class, "replay-tool.war")
                    .addAsLibraries(artifact("org.glassfish:javax.json"))
                    .addAsLibraries(frameworkLibraries.shrinkWrapArchives())
                    .merge(excludeGeneratedApiClasses)
                    .addClass(AsyncStreamDispatcher.class)
                    .addClass(TransactionalEnvelopeDispatcher.class)
                    .addClass(StartReplay.class);
        } catch (Exception e) {
            LOGGER.error("Missing required libraries, unable to create deployable War", e);
            throw e;
        }

    }
}