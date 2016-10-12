package framework.tools.command;

import com.beust.jcommander.JCommander;

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
        final ShellCommand executedCommand = (ShellCommand) commander.getCommands().get(parsedCommand).getObjects().get(0);
        executedCommand.run(args);
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
///Library/Java/JavaVirtualMachines/jdk1.8.0_92.jdk/Contents/Home/lib/tools.jar!/com/sun/jdi/request/EventRequest.class