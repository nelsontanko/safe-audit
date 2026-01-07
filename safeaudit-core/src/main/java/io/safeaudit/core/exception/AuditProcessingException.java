package io.safeaudit.core.exception;

import io.safeaudit.core.domain.AuditEvent;

/**
 * @author Nelson Tanko
 * @since 1.0.0
 */
public class AuditProcessingException extends AuditException {

    private final transient AuditEvent failedEvent;

    public AuditProcessingException(String message, AuditEvent failedEvent) {
        super(message);
        this.failedEvent = failedEvent;
    }

    public AuditProcessingException(String message, AuditEvent failedEvent, Throwable cause) {
        super(message, cause);
        this.failedEvent = failedEvent;
    }

    public AuditEvent getFailedEvent() {
        return failedEvent;
    }
}