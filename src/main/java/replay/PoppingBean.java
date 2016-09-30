package replay;

import org.jboss.weld.environment.se.bindings.Parameters;
import org.jboss.weld.environment.se.events.ContainerInitialized;

import java.util.List;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;


@Singleton
public class PoppingBean {

    @Inject
    private GreenBean greenBean;

    public void printHello(@Observes ContainerInitialized event, @Parameters List<String> parameters) {

        System.out.println("Popping parameters: " + parameters.get(0));

        greenBean.start();
    }
}
