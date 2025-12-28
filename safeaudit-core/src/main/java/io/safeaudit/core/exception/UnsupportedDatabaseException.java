package io.safeaudit.core.exception;

/**
 * @author Nelson Tanko
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