package uk.gov.justice.framework.tools.replay;

import uk.gov.justice.services.event.buffer.core.repository.streamstatus.StreamStatus;
import uk.gov.justice.services.event.buffer.core.repository.streamstatus.StreamStatusJdbcRepository;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.UUID;
import java.util.concurrent.Future;
import java.util.stream.Stream;

import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Stateless
public class AsyncStreamDispatcher {
    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncStreamDispatcher.class);

    @Inject
    private TransactionalEnvelopeDispatcher dispatcher;

    @Inject
    private StreamStatusJdbcRepository streamStatusRepository;

    @Asynchronous
    public Future<Void> dispatch(final Stream<JsonEnvelope> stream) {
        final int[] noOfProcessedElements = {0};
        final JsonEnvelope[] envelope = {null};

        try (final Stream<JsonEnvelope> stream1 = stream) {
            stream1.forEach(e -> {
                if (firstElement(noOfProcessedElements)) {
                    LOGGER.info("Starting processing of stream: {}", streamIdOf(e));
                }
                dispatcher.dispatch(e);
                noOfProcessedElements[0]++;
                if (shouldLogProgress(noOfProcessedElements)) {
                    LOGGER.info("Processed {} elements of stream: {}", noOfProcessedElements[0], streamIdOf(e));
                }
                envelope[0] = e;
            });

            streamStatusRepository.insert(new StreamStatus(streamIdOf(envelope[0]), versionOf(envelope[0])));
            LOGGER.info("Finished processing of stream: {}, elements processed: {}", streamIdOf(envelope[0]), noOfProcessedElements[0]);
        }
        return new AsyncResult<>(null);

    }

    private UUID streamIdOf(final JsonEnvelope envelope) {
        return envelope.metadata().streamId()
                .orElseThrow(() -> new IllegalArgumentException(String.format("Stream id not found in the envelope: %s", envelope.toString())));
    }

    private Long versionOf(final JsonEnvelope envelope) {
        return envelope.metadata().version()
                .orElseThrow(() -> new IllegalArgumentException(String.format("Version not found in the envelope: %s", envelope.toString())));
    }

    private boolean shouldLogProgress(final int[] i) {
        return i[0] % 100 == 0;
    }

    private boolean firstElement(final int[] i) {
        return i[0] == 0;
    }
}