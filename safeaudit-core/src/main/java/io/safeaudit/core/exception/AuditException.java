package io.safeaudit.core.exception;

/**
 * @author Nelson Tanko
 * @since 1.0.0
 */
public class AuditException extends RuntimeException {

    public AuditException(String message) {
        super(message);
    }

    public AuditException(String message, Throwable cause) {
        super(message, cause);
    }

    public AuditException(Throwable cause) {
        super(cause);
    }
}