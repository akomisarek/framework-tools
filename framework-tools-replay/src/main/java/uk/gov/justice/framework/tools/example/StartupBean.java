package uk.gov.justice.framework.tools.example;

import uk.gov.justice.external.ExBean;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

@Singleton
@Startup
public class StartupBean {

    @Inject
    ExBean exBean;

    @PostConstruct
    public void initialise() {
        System.out.println("--------------Example Bean initialised by IOC");
        exBean.sing();

    }


}