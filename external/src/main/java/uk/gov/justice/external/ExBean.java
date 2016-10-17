package uk.gov.justice.external;


import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ExBean {
    public void sing() {
        System.out.println("!!!!external bean singing: I have been injected!");
    }
}
