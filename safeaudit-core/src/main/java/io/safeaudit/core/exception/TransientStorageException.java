package io.safeaudit.core.exception;

/**
 * @author Nelson Tanko
 * @since 1.0.0
 */
public class TransientStorageException extends AuditStorageException {

    public TransientStorageException(String message) {
        super(message);
    }

    public TransientStorageException(String message, Throwable cause) {
        super(message, cause);
    }

    public TransientStorageException(Throwable cause) {
        super(cause);
    }
}