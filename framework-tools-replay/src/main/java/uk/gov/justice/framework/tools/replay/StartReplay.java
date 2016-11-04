package uk.gov.justice.framework.tools.replay;

import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;
import static uk.gov.justice.services.core.annotation.ServiceComponentLocation.LOCAL;

import uk.gov.justice.services.core.dispatcher.Dispatcher;
import uk.gov.justice.services.core.dispatcher.DispatcherCache;
import uk.gov.justice.services.eventsourcing.repository.jdbc.JdbcEventRepository;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import org.slf4j.Logger;

@Singleton
@Startup
public class StartReplay {

    @Inject
    Logger logger;

    @Inject
    DispatcherCache dispatcherCache;

    @Inject
    JdbcEventRepository jdbcEventRepository;

    @PostConstruct
    public void initialise() {
        logger.info("-------------- Replay Event Streams --------------");

        final Dispatcher dispatcher = dispatcherCache.dispatcherFor(EVENT_LISTENER, LOCAL);

        try (final Stream<JsonEnvelope> envelopeStream = jdbcEventRepository.getAll()) {
            envelopeStream
                    .peek(jsonEnvelope -> logger.info(jsonEnvelope.toString()))
                    .forEach(dispatcher::dispatch);
        }

        logger.info("-------------- Replay of Event Streams Complete --------------");
        logger.info("--------------        Press Ctrl+C to exit      --------------");
    }

}