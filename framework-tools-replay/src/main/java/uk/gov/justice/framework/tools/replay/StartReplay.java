package uk.gov.justice.framework.tools.replay;

import static java.lang.System.currentTimeMillis;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;
import static uk.gov.justice.services.core.annotation.ServiceComponentLocation.LOCAL;

import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.ServiceComponentLocation;
import uk.gov.justice.services.core.dispatcher.Dispatcher;
import uk.gov.justice.services.core.dispatcher.DispatcherCache;
import uk.gov.justice.services.eventsourcing.repository.jdbc.JdbcEventRepository;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Startup
public class StartReplay {

    private static final Logger LOGGER = LoggerFactory.getLogger(StartReplay.class);

    @Inject
    private JdbcEventRepository jdbcEventRepository;

    @Inject
    private AsyncStreamDispatcher asyncStreamDispatcher;

    boolean started = false;

    private final Deque<Future<Void>> dispatchResults = new LinkedList<>();


    @PostConstruct
    void go() throws InterruptedException {
        final long startTimeMillis = currentTimeMillis();
        LOGGER.info("-------------- Replay Event Streams -------------!");

        try (final Stream<Stream<JsonEnvelope>> streamOfEventStreams = jdbcEventRepository.getStreamOfAllEventStreams()) {
            streamOfEventStreams.forEach(s -> {
                dispatchResults.add(asyncStreamDispatcher.dispatch(s));
            });
            started = true;

            while (!finished()) {
                Thread.sleep(3000);
            }
        }

        LOGGER.info("-------------- Replay of Event Streams Completed in {} milliseconds--------------", currentTimeMillis() - startTimeMillis);
    }


    boolean finished() {
        final Iterator<Future<Void>> iterator = dispatchResults.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().isDone()) {
                iterator.remove();
            }
        }
        return started && !dispatchResults.stream().filter(f -> !f.isDone()).findAny().isPresent();
    }
}