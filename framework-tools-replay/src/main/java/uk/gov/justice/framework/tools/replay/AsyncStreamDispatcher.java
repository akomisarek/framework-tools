package uk.gov.justice.framework.tools.replay;

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
    TransactionalEnvelopeDispatcher dispatcher;

    @Asynchronous
    public Future<Void> dispatch(final Stream<JsonEnvelope> stream) {
        final int[] i = {0};
        final UUID[] streamId = {null};

        try (final Stream<JsonEnvelope> stream1 = stream) {
            stream1.forEach(e -> {
                if (firstElement(i)) {
                    streamId[0] = streamIdOf(e);
                    LOGGER.info("Starting processing of stream: {}", streamId[0]);
                }
                dispatcher.dispatch(e);
                i[0]++;
                if (shouldLogProgress(i)) {
                    LOGGER.info("Processed {} elements of stream: {}", i[0], streamId[0]);
                }
            });
            LOGGER.info("Finished processing of stream: {}, elements processed: {}", streamId[0], i[0]);
        }

        return new AsyncResult<>(null);

    }

    private UUID streamIdOf(final JsonEnvelope e) {
        return e.metadata().streamId().get();
    }

    private boolean shouldLogProgress(final int[] i) {
        return i[0] % 100 == 0;
    }

    private boolean firstElement(final int[] i) {
        return i[0] == 0;
    }
}
