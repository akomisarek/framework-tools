package uk.gov.justice.framework.tools.replay;

import static com.jayway.awaitility.Awaitility.await;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;
import static uk.gov.justice.services.core.annotation.ServiceComponentLocation.LOCAL;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelope;

import uk.gov.justice.services.core.dispatcher.Dispatcher;
import uk.gov.justice.services.core.dispatcher.DispatcherCache;
import uk.gov.justice.services.eventsourcing.repository.jdbc.JdbcEventRepository;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

import javax.ejb.AsyncResult;

import com.jayway.awaitility.Awaitility;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class StartReplayTest {

    @Mock
    private AsyncStreamDispatcher dispatcher;

    @Mock
    private JdbcEventRepository jdbcEventRepository;

    @InjectMocks
    private StartReplay startReplay;

    @Test
    public void shouldDispatchStreams() throws Exception {
        final Stream<JsonEnvelope> stream1 = Stream.of(envelope().build());
        final Stream<JsonEnvelope> stream2 = Stream.of(envelope().build());


        when(jdbcEventRepository.getStreamOfAllEventStreams()).thenReturn(Stream.of(stream1, stream2));
        when(dispatcher.dispatch(stream1)).thenReturn(new TestFuture(true));
        when(dispatcher.dispatch(stream2)).thenReturn(new TestFuture(true));

        startReplay.go();


        verify(dispatcher).dispatch(stream1);
        verify(dispatcher).dispatch(stream2);
    }

    @Test
    public void shouldReturnTrueWhenAllStreamsFinishedProcessing() throws Exception {
        final Stream<JsonEnvelope> stream1 = Stream.of(envelope().build());
        final Stream<JsonEnvelope> stream2 = Stream.of(envelope().build());


        when(jdbcEventRepository.getStreamOfAllEventStreams()).thenReturn(Stream.of(stream1, stream2));
        when(dispatcher.dispatch(stream1)).thenReturn(new TestFuture(true));
        when(dispatcher.dispatch(stream2)).thenReturn(new TestFuture(true));

        startReplay.go();

        assertTrue(startReplay.finished());

    }

    @Test
    public void shouldReturnFalseWhenOneStreamHasNotFinishedProcessing() throws Exception {
        final Stream<JsonEnvelope> stream1 = Stream.of(envelope().build());
        final Stream<JsonEnvelope> stream2 = Stream.of(envelope().build());


        when(jdbcEventRepository.getStreamOfAllEventStreams()).thenReturn(Stream.of(stream1, stream2));
        when(dispatcher.dispatch(stream1)).thenReturn(new TestFuture(true));
        final TestFuture stream2ProcessingResult = new TestFuture(false);
        when(dispatcher.dispatch(stream2)).thenReturn(stream2ProcessingResult);

        new Thread(() -> {
            try {
                startReplay.go();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        await().until(() -> startReplay.started);

        assertFalse(startReplay.finished());

        stream2ProcessingResult.setDone(true);

        assertTrue(startReplay.finished());
    }

    private static class TestFuture implements Future<Void> {

        private boolean done;

        public void setDone(final boolean done) {
            this.done = done;
        }

        public TestFuture(final boolean done) {
            this.done = done;
        }

        @Override
        public boolean cancel(final boolean mayInterruptIfRunning) {
            return false;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean isDone() {
            return done;
        }

        @Override
        public Void get() throws InterruptedException, ExecutionException {
            return null;
        }

        @Override
        public Void get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return null;
        }
    }

}