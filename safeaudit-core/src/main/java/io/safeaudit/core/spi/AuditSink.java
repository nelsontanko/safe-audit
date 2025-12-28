package io.safeaudit.core.spi;

import io.safeaudit.core.domain.AuditEvent;
import io.safeaudit.core.domain.HealthStatus;

import java.util.List;

/**
 * External sink for audit events (e.g., Kafka, file, SIEM).
 * Sinks are best-effort - failures should not block the main audit pipeline.
 *
 * @author Nelson Tanko
 */
public interface AuditSink {

    /**
     * Send a single audit event to external sink.
     * Should be non-blocking and handle failures gracefully.
     *
     * @param event the audit event
     * @return true if sent successfully
     */
    boolean send(AuditEvent event);

    /**
     * Send multiple events in batch (optional optimization).
     *
     * @param events the list of events
     * @return number of events sent successfully
     */
    default int sendBatch(List<AuditEvent> events) {
        int sent = 0;
        for (AuditEvent event : events) {
            if (send(event)) {
                sent++;
            }
        }
        return sent;
    }

    /**
     * Check sink health.
     *
     * @return health status
     */
    HealthStatus checkHealth();

    /**
     * Close resources used by this sink.
     */
    default void close() {
        // Default: no-op
    }
}
