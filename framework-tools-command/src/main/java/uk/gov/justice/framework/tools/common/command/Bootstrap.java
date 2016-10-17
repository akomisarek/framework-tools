package uk.gov.justice.framework.tools.common.command;

import com.beust.jcommander.JCommander;

import org.reflections.Reflections;

import java.lang.reflect.Modifier;
import java.util.Set;


import static uk.gov.justice.framework.tools.common.command.helper.JCommanderUtils.createInstanceAndAddToJCommander;

public class Bootstrap {

    public static void main(String ... args) throws Exception {

        final Reflections reflections = new Reflections("uk.gov.justice.framework.tools");
        final Set<Class<? extends ShellCommand>> subTypes = reflections.getSubTypesOf(ShellCommand.class);

        final JCommander commander = new JCommander();
        subTypes.stream().forEach(commandClass -> {
            if(!Modifier.isAbstract(commandClass.getModifiers())) {
                createInstanceAndAddToJCommander(commander, commandClass);
            }
        });

        commander.parse(args);
        final String parsedCommand = commander.getParsedCommand();
        final ShellCommand executedCommand = (ShellCommand) commander.getCommands().get(parsedCommand).getObjects().get(0);
        executedCommand.run(args);
    }
}