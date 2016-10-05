package tooling.command.shell;

/**
 * Created by justin on 05/10/2016.
 */
public class ExampleBean {

    private String testValue;

    public ExampleBean() {
        System.out.println("Example Bean Created by IOC");
    }

    public void setTestValue(String testValue) {
        System.out.println("Test value set in Example Bean from CMD parameters");
        this.testValue = testValue;
    }

    public String getTestValue() {
        return testValue;
    }
}
