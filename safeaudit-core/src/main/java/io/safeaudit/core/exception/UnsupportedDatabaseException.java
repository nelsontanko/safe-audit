package io.safeaudit.core.exception;

/**
 * @author Nelson Tanko
 * @since 1.0.0
 */
public class UnsupportedDatabaseException extends AuditConfigurationException {

    private final String databaseType;

    public UnsupportedDatabaseException(String databaseType) {
        super("Unsupported database type: " + databaseType);
        this.databaseType = databaseType;
    }

    public String getDatabaseType() {
        return databaseType;
    }
}