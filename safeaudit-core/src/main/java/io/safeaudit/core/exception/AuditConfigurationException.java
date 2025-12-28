package io.safeaudit.core.exception;

/**
 * @author Nelson Tanko
 */
public class AuditConfigurationException extends AuditException {

    public AuditConfigurationException(String message) {
        super(message);
    }

    public AuditConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}