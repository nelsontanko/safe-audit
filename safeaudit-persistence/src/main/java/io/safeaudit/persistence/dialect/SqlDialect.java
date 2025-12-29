package io.safeaudit.persistence.dialect;

/**
 * @author Nelson Tanko
 */
public interface SqlDialect {

    /**
     * Get database type.
     */
    String getDatabaseType();

    /**
     * Generate table creation DDL.
     */
    String createTableDDL(String tableName);

    /**
     * Generate partition creation DDL (if supported).
     */
    String createPartitionDDL(String tableName, String partitionName, String fromValue, String toValue);

    /**
     * Generate insert statement.
     */
    String insertSQL(String tableName);

    /**
     * Generate select by ID statement.
     */
    String selectByIdSQL(String tableName);

    /**
     * Generate count statement with criteria.
     */
    String countSQL(String tableName, boolean hasWhere);

    /**
     * Generate select statement with pagination.
     */
    String selectSQL(String tableName, String whereClause, String orderBy, int limit, int offset);

    /**
     * Check if database supports partitioning.
     */
    boolean supportsPartitioning();

    /**
     * Get the SQL type for JSON columns.
     */
    String getJsonType();

    /**
     * Get the SQL type for timestamp with timezone.
     */
    String getTimestampType();

    /**
     * Get the SQL type for UUID.
     */
    String getUuidType();
}
