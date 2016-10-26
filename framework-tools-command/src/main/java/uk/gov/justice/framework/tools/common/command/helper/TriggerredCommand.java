package uk.gov.justice.framework.tools.common.command.helper;

import uk.gov.justice.framework.tools.common.command.ShellCommand;

public class TriggerredCommand {

    private static ShellCommand firedCommand = null;
    private static TriggerredCommand instance = null;

    private TriggerredCommand(ShellCommand firedCommand) {
        this.firedCommand = firedCommand;
    }

    public static TriggerredCommand getInstance() {
        if (instance == null) {
        }
        return instance;
    }

    public static ShellCommand getFiredCommand() {
        return firedCommand;
    }

    public static void setFiredCommand(ShellCommand firedCommand) {
        TriggerredCommand.firedCommand = firedCommand;
    }
}

