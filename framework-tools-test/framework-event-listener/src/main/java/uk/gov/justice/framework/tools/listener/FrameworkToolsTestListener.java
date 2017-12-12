package uk.gov.justice.framework.tools.listener;

import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(value = Component.EVENT_LISTENER)
public class FrameworkToolsTestListener {

    private static final Logger logger = LoggerFactory.getLogger(FrameworkToolsTestListener.class);

    @Handles("framework.example-test")
    public void handle(final JsonEnvelope envelope) {
        logger.info("caught a fish!");
    }
}
