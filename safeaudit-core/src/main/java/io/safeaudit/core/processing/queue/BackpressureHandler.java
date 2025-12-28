package io.safeaudit.core.processing.queue;

import io.safeaudit.core.config.AuditProperties;
import io.safeaudit.core.domain.AuditEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles backpressure when audit queue is full.
 *
 * @author Nelson Tanko
 */
public record BackpressureHandler(
        AuditProperties.BackpressureStrategy strategy,
        int threshold
) {

    private static final Logger log = LoggerFactory.getLogger(BackpressureHandler.class);

    public void handle(AuditEvent event, int currentQueueSize) {
        if (currentQueueSize < threshold) {
            return;
        }

        switch (strategy) {
            case DROP_OLDEST:
                log.warn("Queue at capacity, event dropped: {}", event.eventId());
                break;

            case BLOCK:
                log.warn("Queue at capacity, blocking caller");
                // Caller will block on queue.offer()
                break;

            case REJECT:
                throw new IllegalStateException(
                        "Audit queue full, rejecting event: " + event.eventId()
                );
        }
    }
}
