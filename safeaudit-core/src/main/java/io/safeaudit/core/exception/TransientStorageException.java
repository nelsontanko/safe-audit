package io.safeaudit.core.exception;

/**
 * @author Nelson Tanko
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