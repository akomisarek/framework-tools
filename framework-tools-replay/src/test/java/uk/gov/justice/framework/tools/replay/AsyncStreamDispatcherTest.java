package uk.gov.justice.framework.tools.replay;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.services.core.handler.exception.MissingHandlerException;
import uk.gov.justice.services.event.buffer.core.repository.streamstatus.StreamStatus;
import uk.gov.justice.services.event.buffer.core.repository.streamstatus.StreamStatusJdbcRepository;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static java.util.UUID.randomUUID;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.*;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithDefaults;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.metadata;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelope;

@RunWith(MockitoJUnitRunner.class)
public class AsyncStreamDispatcherTest {

    @Mock
    private TransactionalEnvelopeDispatcher envelopeDispatcher;
    @Mock
    private StreamStatusJdbcRepository streamStatusRepository;

    @InjectMocks
    private AsyncStreamDispatcher asyncStreamDispatcher;

    @Test
    public void shouldDispatchEnvelopes() {

        final UUID streamId = randomUUID();
        final JsonEnvelope envelope1 = envelope().with(metadataWithDefaults().withStreamId(streamId).withVersion(1L)).build();
        final JsonEnvelope envelope2 = envelope().with(metadataWithDefaults().withStreamId(streamId).withVersion(2L)).build();

        doNothing().when(envelopeDispatcher).dispatch(envelope1);
        doNothing().when(envelopeDispatcher).dispatch(envelope2);

        asyncStreamDispatcher.dispatch(Stream.of(envelope1, envelope2));

        final ArgumentCaptor<JsonEnvelope> dispatchCaptor = ArgumentCaptor.forClass(JsonEnvelope.class);

        verify(envelopeDispatcher, times(2)).dispatch(dispatchCaptor.capture());
        final List<JsonEnvelope> dispatchedEnvelopes = dispatchCaptor.getAllValues();

        assertThat(dispatchedEnvelopes, contains(envelope1, envelope2));
    }

    @Test
    public void shouldUpdateStreamBufferStatus() {

        final UUID streamId = randomUUID();
        final JsonEnvelope envelope1 = envelope().with(metadataWithDefaults().withStreamId(streamId).withVersion(4L)).build();
        final JsonEnvelope envelope2 = envelope().with(metadataWithDefaults().withStreamId(streamId).withVersion(5L)).build();

        asyncStreamDispatcher.dispatch(Stream.of(envelope1, envelope2));

        verify(streamStatusRepository).insert(new StreamStatus(streamId, 5L));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionIfNoStreamIdInTheEnvelope() {
        final Stream<JsonEnvelope> stream = Stream.of(envelope().with(metadataWithRandomUUID("dummyName").withVersion(1L)).build());
        asyncStreamDispatcher.dispatch(stream);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionIfNoVersionInTheEnvelope() {
        final Stream<JsonEnvelope> stream = Stream.of(envelope().with(metadataWithDefaults().withStreamId(randomUUID())).build());
        asyncStreamDispatcher.dispatch(stream);
    }

    @Test
    public void shouldProcessStreamWhenThereIsNoHandlerDefined() {

        final UUID streamId = randomUUID();

        final JsonEnvelopeMatcher envelopeMatcherForEventWithoutHandler = jsonEnvelope()
                .withMetadataOf(metadata().withName("event-without-handler"));

        doThrow(new MissingHandlerException("Handler for event-without-handler not found"))
                .when(envelopeDispatcher).dispatch(argThat(envelopeMatcherForEventWithoutHandler));


        final JsonEnvelope envelope1 = envelope().with(metadataWithRandomUUID("event-with-handler")
                                                 .withStreamId(streamId)
                                                 .withVersion(1L))
                                                 .build();
        final JsonEnvelope envelope2 = envelope().with(metadataWithRandomUUID("event-without-handler")
                                                 .withStreamId(streamId)
                                                 .withVersion(2L)).build();
        final JsonEnvelope envelope3 = envelope().with(metadataWithRandomUUID("event-with-handler").withStreamId(streamId).withVersion(3L)).build();

        asyncStreamDispatcher.dispatch(Stream.of(envelope1, envelope2, envelope3));

        ArgumentCaptor<JsonEnvelope> dispatchCaptor = ArgumentCaptor.forClass(JsonEnvelope.class);

        verify(envelopeDispatcher, times(3)).dispatch(dispatchCaptor.capture());
        verify(streamStatusRepository).insert(new StreamStatus(streamId, 3L));

    }


}