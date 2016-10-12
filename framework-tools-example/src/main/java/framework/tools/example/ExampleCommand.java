package framework.tools.example;

import com.beust.jcommander.Parameter;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset;
import org.wildfly.swarm.Swarm;
import org.wildfly.swarm.undertow.WARArchive;

import java.net.URL;


@com.beust.jcommander.Parameters(separators = "=", commandDescription = "A test command")
public class ExampleCommand implements ShellCommand {


    @Parameter(names = "-test", description = "a test value")
    private String testValue;

    public String getTestValue() {
        return testValue;
    }

    /*
        If you intend to bootstrap in the jee container use the below
        and create a handler which observes this command as an event

        If you don't wish to use jee container then use this method to
        trigger your application, create an instance etc
     */
    public void run(String[] args) {

        try {
            Swarm swarm = new Swarm();

            ClassLoader cl = Main.class.getClassLoader();
            URL stageConfig = cl.getResource("project-stages.yml");

            swarm.withStageConfig(stageConfig);

            swarm.start();

            WARArchive deployment = ShrinkWrap.create(WARArchive.class);
            deployment.addPackage(ExampleCommand.class.getPackage());
            deployment.addAsWebInfResource(new ClassLoaderAsset("META-INF/beans.xml", Main.class.getClassLoader()), "classes/META-INF/beans.xml");
            deployment.addAsWebInfResource(new ClassLoaderAsset("META-INF/services/javax.enterprise.inject.spi.Extension", Main.class.getClassLoader()), "classes/META-INF/services/javax.enterprise.inject.spi.Extension");
            deployment.addAllDependencies();

            System.out.println(deployment.toString(true));

            swarm.deploy(deployment);

//            BeanManager beanManager = CDI.current().getBeanManager();
//            beanManager.fireEvent(this);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
