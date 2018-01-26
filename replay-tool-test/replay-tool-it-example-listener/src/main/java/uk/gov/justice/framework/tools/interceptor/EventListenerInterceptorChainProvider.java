package uk.gov.justice.framework.tools.interceptor;

import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import uk.gov.justice.services.components.event.listener.interceptors.EventBufferInterceptor;
import uk.gov.justice.services.components.event.listener.interceptors.EventFilterInterceptor;
import uk.gov.justice.services.core.interceptor.InterceptorChainEntry;
import uk.gov.justice.services.core.interceptor.InterceptorChainEntryProvider;
import uk.gov.justice.services.metrics.interceptor.IndividualActionMetricsInterceptor;
import uk.gov.justice.services.metrics.interceptor.TotalActionMetricsInterceptor;

import java.util.LinkedList;
import java.util.List;

public class EventListenerInterceptorChainProvider implements InterceptorChainEntryProvider {

    final List<InterceptorChainEntry> interceptorChainEntries = new LinkedList<>();

    public EventListenerInterceptorChainProvider() {
        interceptorChainEntries.add(new InterceptorChainEntry(1, TotalActionMetricsInterceptor.class));
        interceptorChainEntries.add(new InterceptorChainEntry(2, IndividualActionMetricsInterceptor.class));
        interceptorChainEntries.add(new InterceptorChainEntry(1000, EventBufferInterceptor.class));
        interceptorChainEntries.add(new InterceptorChainEntry(2000, EventFilterInterceptor.class));

    }

    @Override
    public String component() {
        return EVENT_LISTENER;
    }

    @Override
    public List<InterceptorChainEntry> interceptorChainTypes() {
        return interceptorChainEntries;
    }
}
