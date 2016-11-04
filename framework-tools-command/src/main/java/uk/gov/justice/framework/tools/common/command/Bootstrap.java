package uk.gov.justice.framework.tools.common.command;

import static java.lang.Class.forName;
import static java.lang.reflect.Modifier.isAbstract;

import java.util.Optional;
import java.util.Set;

import com.beust.jcommander.JCommander;
import org.reflections.Reflections;

public class Bootstrap {

    private final JCommander commander;

    private Bootstrap() {
        this.commander = new JCommander();
        this.commander.setAcceptUnknownOptions(true);
    }

    public static void main(final String... args) throws Exception {
        new Bootstrap().setup(args);
    }

    private void setup(final String[] args) {
        final Reflections reflections = new Reflections("uk.gov.justice.framework.tools");
        final Set<Class<? extends ShellCommand>> subTypes = reflections.getSubTypesOf(ShellCommand.class);

        subTypes.stream()
                .filter(this::commandClassIsNotAbstract)
                .forEach(this::createInstanceAndAddToJCommander);

        commander.parse(args);

        getParsedCommand().ifPresent(command -> ((ShellCommand) command).run(args));
    }

    private Optional<Object> getParsedCommand() {
        return commander
                .getCommands()
                .get(commander.getParsedCommand())
                .getObjects().stream()
                .findFirst();
    }

    private boolean commandClassIsNotAbstract(final Class<? extends ShellCommand> commandClass) {
        return !isAbstract(commandClass.getModifiers());
    }

    private void createInstanceAndAddToJCommander(final Class<? extends ShellCommand> commandClass) {
        try {
            commander.addCommand(commandClass.getSimpleName().toLowerCase(), forName(commandClass.getName()).newInstance());
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}