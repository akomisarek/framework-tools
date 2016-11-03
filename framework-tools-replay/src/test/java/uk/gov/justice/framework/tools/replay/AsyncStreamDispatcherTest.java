package uk.gov.justice.framework.tools.replay;


import static java.util.UUID.randomUUID;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelope;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithDefaults;

import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.JsonObjectMetadata;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AsyncStreamDispatcherTest {

    @Mock
    private TransactionalEnvelopeDispatcher envelopeDispatcher;

    @InjectMocks
    private AsyncStreamDispatcher asyncStreamDispatcher;

    @Test
    public void shouldDispatchEnvelopes() throws Exception {

        final JsonEnvelope envelope1 = envelope().with(metadataWithDefaults().withStreamId(randomUUID())).build();
        final JsonEnvelope envelope2 = envelope().with(metadataWithDefaults().withStreamId(randomUUID())).build();

        asyncStreamDispatcher.dispatch(Stream.of(envelope1, envelope2));


        ArgumentCaptor<JsonEnvelope> dispatchCaptor = ArgumentCaptor.forClass(JsonEnvelope.class);

        verify(envelopeDispatcher, times(2)).dispatch(dispatchCaptor.capture());
        final List<JsonEnvelope> dispatchedEnvelopes = dispatchCaptor.getAllValues();

        assertThat(dispatchedEnvelopes, contains(envelope1, envelope2));

    }
}