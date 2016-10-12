package framework.tools.example;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.inject.Inject;

@Stateless
public class ExampleHandler implements Extension{


    public void afterDeploymentValidation(@Observes final AfterBeanDiscovery event, final BeanManager beanManager) {
        System.out.println("----------AfterDeploymentValidation event--------");
        beanManager.fireEvent(this);
    }

}
