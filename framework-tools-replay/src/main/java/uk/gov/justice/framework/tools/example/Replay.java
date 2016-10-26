package uk.gov.justice.framework.tools.example;

import uk.gov.justice.framework.tools.swarm.command.SwarmCommand;

import java.io.File;
import java.nio.file.Path;

import com.beust.jcommander.Parameter;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.wildfly.swarm.Swarm;
import org.wildfly.swarm.datasources.DatasourcesFraction;


@com.beust.jcommander.Parameters(separators = "=", commandDescription = "A test command")
public class Replay extends SwarmCommand {


    @Parameter(names = "-l", description = "external library")
    private Path library;


    public void run(String[] args) {
        System.out.println("--->!!!!!!!!!Replay command executed, xmlConfigurationPath value " + xmlConfigurationPath);
        try {

            final Swarm swarm = new Swarm(firedArgs());
                    //.withXmlConfig(xmlConfig);
            swarm.fraction(new DatasourcesFraction());
            swarm.start();
            WebArchive deployment = ShrinkWrap.create(WebArchive.class, "myapp1.war");
            deployment.addClass(StartupBean.class);
            deployment.addAsLibraries(new File(library.toUri()));

            swarm.deploy(deployment);

            //swarm.deploy();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}