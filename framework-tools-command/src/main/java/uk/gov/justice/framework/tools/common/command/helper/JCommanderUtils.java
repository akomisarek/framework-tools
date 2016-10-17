package uk.gov.justice.framework.tools.common.command.helper;

import com.beust.jcommander.JCommander;

import uk.gov.justice.framework.tools.common.command.ShellCommand;

import static java.lang.Class.forName;

public class JCommanderUtils {

    public static void createInstanceAndAddToJCommander(JCommander commander, Class<? extends ShellCommand> aClass) {
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
