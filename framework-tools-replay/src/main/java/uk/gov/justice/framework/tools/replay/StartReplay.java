package uk.gov.justice.framework.tools.replay;

import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;
import static uk.gov.justice.services.core.annotation.ServiceComponentLocation.LOCAL;

import uk.gov.justice.services.core.dispatcher.Dispatcher;
import uk.gov.justice.services.core.dispatcher.DispatcherCache;
import uk.gov.justice.services.eventsourcing.repository.jdbc.JdbcEventRepository;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

@Singleton
@Startup
public class StartReplay {

    @Inject
    DispatcherCache dispatcherCache;

    @Inject
    JdbcEventRepository jdbcEventRepository;

    @PostConstruct
    public void initialise() {
        System.out.println("-------------- Replay Event Streams --------------");

        final Dispatcher dispatcher = dispatcherCache.dispatcherFor(EVENT_LISTENER, LOCAL);

        jdbcEventRepository.getAll()
                .peek(System.out::println)
                .forEach(dispatcher::dispatch);

        System.out.println("-------------- Replay of Event Streams Complete --------------");
        System.out.println("--------------        Press Ctrl+C to exit      --------------");
    }

}