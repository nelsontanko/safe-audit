package io.safeaudit.core.util;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

import java.time.Duration;

/**
 * @author Nelson Tanko
 */
public class AuditMetrics {

    private final MeterRegistry registry;
    private final Counter eventCounter;
    private final Timer processingTimer;
    private final Timer persistenceTimer;

    public AuditMetrics(MeterRegistry registry) {
        this.registry = registry;
        this.eventCounter = io.micrometer.core.instrument.Counter.builder("audit.events")
                .description("Audit events by status")
                .register(registry);

        this.processingTimer = Timer.builder("audit.processing.time")
                .description("Time to process audit event")
                .register(registry);

        this.persistenceTimer = Timer.builder("audit.persistence.time")
                .description("Time to persist audit event")
                .register(registry);
    }

    public void recordEventCaptured(String eventType) {
        eventCounter.increment();
        Counter.builder("audit.events")
                .tag("status", "captured")
                .tag("type", eventType)
                .register(registry)
                .increment();
    }

    public void recordEventPersisted() {
        Counter.builder("audit.events")
                .tag("status", "persisted")
                .register(registry)
                .increment();
    }

    public void recordEventFailed(String reason) {
        Counter.builder("audit.events")
                .tag("status", "failed")
                .tag("reason", reason)
                .register(registry)
                .increment();
    }

    public void recordProcessingTime(Duration duration) {
        processingTimer.record(duration);
    }

    public void recordPersistenceTime(Duration duration) {
        persistenceTimer.record(duration);
    }
}