package tooling.command.shell;

import com.beust.jcommander.Parameter;

@com.beust.jcommander.Parameters(separators = "=", commandDescription = "A test command")
public class ExampleCommand implements ShellCommand {

    @Parameter(names = "-test", description = "a test value")
    private String testValue;

    public String getTestValue() {
        return testValue;
    }
}
