package io.safeaudit.persistence.dialect;

/**
 * Abstract base class for SQL dialects.
 *
 * @author Nelson Tanko
 * @since 1.0.0
 */
public abstract class AbstractSqlDialect implements SqlDialect {

    @Override
    public String selectByIdSQL(String tableName) {
        return "SELECT * FROM %s WHERE event_id = ?".formatted(tableName);
    }

    @Override
    public String createTriggerSQL(String tableName) {
        return null;
    }

    @Override
    public String countSQL(String tableName, boolean hasWhere) {
        return "SELECT COUNT(*) FROM %s %s".formatted(tableName, hasWhere ? "WHERE" : "");
    }

    @Override
    public String selectSQL(String tableName, String whereClause, String orderBy, int limit, int offset) {
        var sql = new StringBuilder("SELECT * FROM ").append(tableName);

        if (whereClause != null && !whereClause.isBlank()) {
            sql.append(" WHERE ").append(whereClause);
        }

        if (orderBy != null && !orderBy.isBlank()) {
            sql.append(" ORDER BY ").append(orderBy);
        }

        if (limit > 0) {
            sql.append(" LIMIT ").append(limit);
            if (offset > 0) {
                sql.append(" OFFSET ").append(offset);
            }
        }

        return sql.toString();
    }
}
