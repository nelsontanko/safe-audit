package io.safeaudit.persistence.dialect;

import io.safeaudit.persistence.DatabaseType;

/**
 * @author Nelson Tanko
 */
public final class SqlDialectFactory {

    private SqlDialectFactory() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static SqlDialect create(DatabaseType databaseType) {
        return switch (databaseType) {
            case POSTGRESQL -> new PostgreSQLDialect();
            case MYSQL -> new MySQLDialect();
            case H2 -> new H2Dialect();
            case ORACLE -> throw new UnsupportedOperationException("Oracle dialect not yet implemented");
            case MSSQL -> throw new UnsupportedOperationException("MSSQL dialect not yet implemented");
        };
    }
}