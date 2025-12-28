package io.safeaudit.core.spi;

import io.safeaudit.core.domain.AuditEvent;

/**
 * Strategy interface for processing audit events.
 * Processors are chained to transform events before storage.
 *
 * @author Nelson Tanko
 */
@FunctionalInterface
public interface AuditEventProcessor {

    /**
     * Process an audit event.
     * Implementations should not mutate the input event but return a new instance.
     *
     * @param event the input event
     * @return the processed event
     */
    AuditEvent process(AuditEvent event);

    /**
     * Get processor order for chaining.
     * Lower values execute first.
     *
     * @return order value
     */
    default int getOrder() {
        return 0;
    }
}
