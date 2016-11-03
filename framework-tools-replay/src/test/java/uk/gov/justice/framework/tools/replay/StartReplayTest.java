package uk.gov.justice.framework.tools.replay;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;
import static uk.gov.justice.services.core.annotation.ServiceComponentLocation.LOCAL;

import uk.gov.justice.services.core.dispatcher.Dispatcher;
import uk.gov.justice.services.core.dispatcher.DispatcherCache;
import uk.gov.justice.services.eventsourcing.repository.jdbc.JdbcEventRepository;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.stream.Stream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class StartReplayTest {

    @Mock
    Dispatcher dispatcher;

    @Mock
    private DispatcherCache dispatcherCache;

    @Mock
    private JdbcEventRepository jdbcEventRepository;

    @InjectMocks
    private StartReplay startReplay;

    @Test
    public void shouldDispatchAllEvents() throws Exception {
        final JsonEnvelope jsonEnvelope_1 = mock(JsonEnvelope.class);
        final JsonEnvelope jsonEnvelope_2 = mock(JsonEnvelope.class);
        final Stream<JsonEnvelope> jsonEnvelopeStream = Stream.of(jsonEnvelope_1, jsonEnvelope_2);

        when(dispatcherCache.dispatcherFor(EVENT_LISTENER, LOCAL)).thenReturn(dispatcher);
        when(jdbcEventRepository.getAll()).thenReturn(jsonEnvelopeStream);

        startReplay.initialise();

        verify(dispatcher).dispatch(jsonEnvelope_1);
        verify(dispatcher).dispatch(jsonEnvelope_2);
    }
}