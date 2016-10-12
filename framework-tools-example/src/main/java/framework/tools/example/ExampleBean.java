package framework.tools.example;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

@ApplicationScoped
public class ExampleBean {

    private String testValue;

    @Inject
    JellyBean jellyBean;

    public ExampleBean() {
        System.out.println("Example Bean Created by IOC");
    }

    public void fire(@Observes ExampleHandler exampleHandler) {
        System.out.println("I just saw an ExampleHander!!!");
        jellyBean.sing();

    }

    public void setTestValue(String testValue) {
        System.out.println("Test value set in Example Bean from CMD parameters");
        this.testValue = testValue;
    }

    public String getTestValue() {
        return testValue;
    }
}
