package uk.gov.justice.framework.tools.replay;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import uk.gov.justice.services.eventsourcing.repository.jdbc.JdbcEventRepository;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.enterprise.concurrent.ManagedExecutorService;
import java.util.Deque;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class StartReplayTest {

    @Mock
    private JdbcEventRepository jdbcEventRepository;
    @Mock
    private ManagedExecutorService executorService;
    @Mock
    private StreamDispatchTask dispatchTask;
    @Mock
    private Throwable throwable;
    @Mock
    private Future<UUID> dispatchTaskFuture;
    @Mock
    private Stream<JsonEnvelope> mockStream;
    @Mock
    private Deque<UUID> outstandingTasks;
    @Mock
    private Logger logger;

    @InjectMocks
    private StartReplay startReplay;

    @Test
    public void shouldDispatchStreams() throws Exception {
        final Stream<Stream<JsonEnvelope>> streamOfStreams = Stream.of(mockStream, mockStream);

        when(jdbcEventRepository.getStreamOfAllEventStreams()).thenReturn(streamOfStreams);
        when(executorService.submit(any(StreamDispatchTask.class))).thenReturn(dispatchTaskFuture);
        when(outstandingTasks.isEmpty()).thenReturn(true);

        startReplay.go();

        verify(executorService, times(2)).submit(any(StreamDispatchTask.class));
        verify(logger).info("========== ALL TASKS HAVE BEEN DISPATCHED -- SHUTDOWN =================");
    }

    @Test
    public void shouldRemoveAbortedTask() {
        startReplay.taskAborted(dispatchTaskFuture, executorService, dispatchTask, throwable);

        verify(outstandingTasks, times(1)).remove(eq(dispatchTaskFuture));
        verify(outstandingTasks, times(1)).isEmpty();
    }

    @Test
    public void shouldRemoveCompletedTask() {
        when(outstandingTasks.isEmpty()).thenReturn(true);

        startReplay.taskDone(dispatchTaskFuture, executorService, dispatchTask, throwable);

        verify(outstandingTasks, times(1)).remove(eq(dispatchTaskFuture));
        verify(outstandingTasks, times(1)).isEmpty();
    }

    @Test
    public void shouldLogTaskStarting() {
        startReplay.taskStarting(dispatchTaskFuture, executorService, dispatchTask);

        verify(logger).debug(eq("Starting Dispatch task"));
    }

    @Test
    public void shouldLogTaskSubmitted() {
        startReplay.taskSubmitted(dispatchTaskFuture, executorService, dispatchTask);

        verify(logger).debug(eq("Submitted Dispatch task"));
    }


}