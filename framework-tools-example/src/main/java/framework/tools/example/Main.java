package framework.tools.example;

import com.beust.jcommander.JCommander;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.reflections.Reflections;
import org.wildfly.swarm.Swarm;
import org.wildfly.swarm.cdi.CDIFraction;
import org.wildfly.swarm.undertow.WARArchive;

import java.util.Set;

import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

import static java.lang.Class.forName;

public class Main {



    public static void main(String ... args) throws Exception {

        final Reflections reflections = new Reflections("framework.tools");
        final Set<Class<? extends ShellCommand>> subTypes = reflections.getSubTypesOf(ShellCommand.class);

        System.out.println("isEmpty subtypes : " + subTypes.isEmpty());

        final JCommander commander = new JCommander();
        subTypes.stream().forEach(commandClass -> toCommand(commander, commandClass));

        System.out.println("Args: " + args[0]);

        commander.parse(args);
        final String parsedCommand = commander.getParsedCommand();
        final ShellCommand executedCommand = (ShellCommand) commander.getCommands().get(parsedCommand).getObjects().get(0);
        executedCommand.run(args);
    }

    private static void toCommand(JCommander commander, Class<? extends ShellCommand> aClass) {
        try {
            System.out.println("aClass.getSimpleName().toLowerCase() : " + aClass.getSimpleName().toLowerCase());
            commander.addCommand(aClass.getSimpleName().toLowerCase(), forName(aClass.getName()).newInstance());
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}