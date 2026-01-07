package io.safeaudit.core.exception;

/**
 * @author Nelson Tanko
 * @since 1.0.0
 */
public class AuditStorageException extends RuntimeException {

    public AuditStorageException(String message) {
        super(message);
    }

    public AuditStorageException(String message, Throwable cause) {
        super(message, cause);
    }

    public AuditStorageException(Throwable cause) {
        super(cause);
    }
}
