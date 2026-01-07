package io.safeaudit.core.spi;

import io.safeaudit.core.domain.AuditEvent;

/**
 * Strategy interface for capturing audit events.
 * Different capture mechanisms (HTTP, method, event) implement this.
 *
 * @author Nelson Tanko
 * @since 1.0.0
 */
@FunctionalInterface
public interface AuditEventCapture {

    /**
     * Capture an audit event for processing.
     * This method should be non-blocking if async mode is enabled.
     *
     * @param event the event to capture
     */
    void capture(AuditEvent event);
}
