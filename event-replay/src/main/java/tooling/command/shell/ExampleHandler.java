package tooling.command.shell;

import javax.enterprise.event.Observes;
import javax.inject.Inject;

public class ExampleHandler {

    @Inject
    ExampleBean exampleBean;

    public void handle(@Observes ExampleCommand event) {
        System.out.println("setting test value in response to event");
        exampleBean.setTestValue(event.getTestValue());

    }
}
