package io.safeaudit.persistence;

/**
 * @author Nelson Tanko
 */
public enum DatabaseType {
    POSTGRESQL("PostgreSQL", "org.postgresql.Driver"),
    MYSQL("MySQL", "com.mysql.cj.jdbc.Driver"),
    ORACLE("Oracle", "oracle.jdbc.OracleDriver"),
    MSSQL("Microsoft SQL Server", "com.microsoft.sqlserver.jdbc.SQLServerDriver"),
    H2("H2", "org.h2.Driver");

    private final String productName;
    private final String driverClassName;

    DatabaseType(String productName, String driverClassName) {
        this.productName = productName;
        this.driverClassName = driverClassName;
    }

    public String getProductName() {
        return productName;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    /**
     * Detect database type from product name.
     */
    public static DatabaseType fromProductName(String productName) {
        if (productName == null) {
            return H2; // Default for tests
        }

        String lower = productName.toLowerCase();

        if (lower.contains("postgresql") || lower.contains("postgres")) {
            return POSTGRESQL;
        } else if (lower.contains("mysql") || lower.contains("mariadb")) {
            return MYSQL;
        } else if (lower.contains("oracle")) {
            return ORACLE;
        } else if (lower.contains("microsoft") || lower.contains("sql server")) {
            return MSSQL;
        } else if (lower.contains("h2")) {
            return H2;
        }

        throw new IllegalArgumentException("Unsupported database: " + productName);
    }
}