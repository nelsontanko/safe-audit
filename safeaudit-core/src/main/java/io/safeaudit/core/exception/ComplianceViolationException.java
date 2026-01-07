package io.safeaudit.core.exception;

/**
 * @author Nelson Tanko
 * @since 1.0.0
 */
public class ComplianceViolationException extends AuditException {

    private final String regulatoryProfile;

    public ComplianceViolationException(String message) {
        this(null, message);
    }

    public ComplianceViolationException(String regulatoryProfile, String message) {
        super(formatMessage(regulatoryProfile, message));
        this.regulatoryProfile = regulatoryProfile;
    }

    public ComplianceViolationException(String regulatoryProfile, String message, Throwable cause) {
        super(formatMessage(regulatoryProfile, message), cause);
        this.regulatoryProfile = regulatoryProfile;
    }

    public String getRegulatoryProfile() {
        return regulatoryProfile;
    }

    private static String formatMessage(String regulatoryProfile, String message) {
        if (regulatoryProfile != null) {
            return String.format("[%s] %s", regulatoryProfile, message);
        }
        return message;
    }
}
