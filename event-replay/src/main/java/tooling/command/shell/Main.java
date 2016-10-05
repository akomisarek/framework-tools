package tooling.command.shell;

import com.beust.jcommander.JCommander;

import org.jboss.weld.environment.se.StartMain;
import org.jboss.weld.environment.se.WeldContainer;
import org.reflections.Reflections;

import java.util.Set;

import static java.lang.Class.forName;

class Main {

    public static void main(String ... args) throws InterruptedException {

        final Reflections reflections = new Reflections();
        final Set<Class<? extends ShellCommand>> subTypes = reflections.getSubTypesOf(ShellCommand.class);

        final JCommander commander = new JCommander();
        subTypes.stream().forEach(commandClass -> toCommand(commander, commandClass));

        commander.parse(args);
        final String parsedCommand = commander.getParsedCommand();

        StartMain startMain = new StartMain(args);
        final WeldContainer container = startMain.go();

        container.event().fire(commander.getCommands().get(parsedCommand).getObjects().get(0));
    }

    private static void toCommand(JCommander commander, Class<? extends ShellCommand> aClass) {
        try {
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
