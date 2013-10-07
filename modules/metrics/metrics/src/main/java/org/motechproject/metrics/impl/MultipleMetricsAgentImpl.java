package org.motechproject.metrics.impl;

import org.motechproject.commons.date.util.DateUtil;
import org.motechproject.event.metrics.MetricsAgent;
import org.motechproject.metrics.MetricsAgentBackend;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Implementation of {@Link MetricsAgent} interface
 */

@Service("metricsAgentService")
public class MultipleMetricsAgentImpl implements MetricsAgent {

    private List<MetricsAgentBackend> metricsAgents;

    public MultipleMetricsAgentImpl() {
    }

    /**
     * Reports an occurrence of metric, incrementing it's count.  Not all implementations
     * may make use of parameters
     *
     * @param metric     The metric being recorded
     * @param parameters Optional parameters related to the event
     */
    @Override
    public void logEvent(String metric, Map<String, String> parameters) {
        for (MetricsAgentBackend agent : getMetricsAgents()) {
            agent.logEvent(metric, parameters);
        }
    }

    /**
     * Reports an occurrence of metric, incrementing it's count.
     *
     * @param metric The metric being recorded
     */
    @Override
    public void logEvent(String metric) {
        for (MetricsAgentBackend agent : getMetricsAgents()) {
            agent.logEvent(metric);
        }
    }

    /**
     * Starts a timer for metric.  Later calls to startTimer without a corresponding call to endTimer for the same
     * metric are ignored
     */
    @Override
    public long startTimer() {
        return DateUtil.now().getMillis();
    }

    /**
     * Ends the timer for metric and records it.  No action is taken if a start timer was not recorded for metric
     *
     * @param metric     The metric being timed
     * @param startTime
     */
    @Override
    public void stopTimer(String metric, Long startTime) {
        long endTime = DateUtil.now().getMillis();
        long executionTime = endTime - startTime;

        for (MetricsAgentBackend agent : getMetricsAgents()) {
            agent.logTimedEvent(metric, executionTime);
        }
    }

    public void addMetricAgent(MetricsAgentBackend agent) {
        if (metricsAgents == null) {
            metricsAgents = new ArrayList<MetricsAgentBackend>();
        }

        metricsAgents.add(agent);
    }

    public void removeMetricAgent(MetricsAgentBackend agent) {
        if (metricsAgents != null) {
            metricsAgents.remove(agent);
        }
    }

    public List<MetricsAgentBackend> getMetricsAgents() {
        if (metricsAgents == null) {
            return new ArrayList<MetricsAgentBackend>();
        }

        return metricsAgents;
    }

    public void setMetricsAgents(List<MetricsAgentBackend> agents) {
        metricsAgents = agents;
    }
}
