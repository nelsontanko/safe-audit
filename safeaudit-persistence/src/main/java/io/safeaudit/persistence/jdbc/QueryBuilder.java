package io.safeaudit.persistence.jdbc;

import io.safeaudit.core.spi.QueryCriteria;
import io.safeaudit.persistence.dialect.SqlDialect;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author Nelson Tanko
 * @since 1.0.0
 */
public class QueryBuilder {

    private static final Set<String> ALLOWED_SORT_COLUMNS = Set.of(
            "event_id", "sequence_number", "event_timestamp", "event_type", "severity",
            "user_id", "username", "ip_address", "resource", "action"
    );

    private final QueryCriteria criteria;
    private final SqlDialect dialect;
    private final String tableName;
    private final List<Object> parameters = new ArrayList<>();
    private final StringBuilder whereClause = new StringBuilder();

    public QueryBuilder(QueryCriteria criteria, SqlDialect dialect, String tableName) {
        this.criteria = criteria;
        this.dialect = dialect;
        this.tableName = SqlTableRegistry.resolve(tableName);
        buildWhereClause();
    }

    private void buildWhereClause() {
        boolean first = true;

        if (criteria.getEventId() != null) {
            appendCondition("event_id = ?", criteria.getEventId(), first);
            first = false;
        }

        if (criteria.getUserId() != null) {
            appendCondition("user_id = ?", criteria.getUserId(), first);
            first = false;
        }

        if (criteria.getUsername() != null) {
            appendCondition("username LIKE ?", "%" + criteria.getUsername() + "%", first);
            first = false;
        }

        if (criteria.getResource() != null) {
            appendCondition("resource LIKE ?", "%" + criteria.getResource() + "%", first);
            first = false;
        }

        if (criteria.getEventType() != null) {
            appendCondition("event_type = ?", criteria.getEventType(), first);
            first = false;
        }

        if (criteria.getTenantId() != null) {
            appendCondition("tenant_id = ?", criteria.getTenantId(), first);
            first = false;
        }

        if (criteria.getFrom() != null) {
            appendCondition("event_timestamp >= ?", Timestamp.from(criteria.getFrom()), first);
            first = false;
        }

        if (criteria.getTo() != null) {
            appendCondition("event_timestamp <= ?", Timestamp.from(criteria.getTo()), first);
            first = false;
        }

        if (!criteria.getSeverities().isEmpty()) {
            var placeholders = String.join(",",
                    Collections.nCopies(criteria.getSeverities().size(), "?"));
            appendCondition("severity IN (" + placeholders + ")", null, first);
            criteria.getSeverities().forEach(s -> parameters.add(s.name()));
        }
    }

    private void appendCondition(String condition, Object value, boolean first) {
        if (!first) {
            whereClause.append(" AND ");
        }
        whereClause.append(condition);
        if (value != null) {
            parameters.add(value);
        }
    }

    public String buildSelectSQL() {
        var sortColumn = resolveSortColumn(criteria.getSortBy());
        var orderBy = sortColumn + " " + criteria.getSortDirection().name();
        return dialect.selectSQL(
                tableName,
                whereClause.toString(),
                orderBy,
                criteria.getSize(),
                criteria.getPage() * criteria.getSize()
        );
    }

    private String resolveSortColumn(String sortBy) {
        if (sortBy == null || sortBy.isBlank()) {
            return "event_timestamp";
        }
        if (!ALLOWED_SORT_COLUMNS.contains(sortBy)) {
            throw new IllegalArgumentException("Invalid sort column: " + sortBy);
        }
        return sortBy;
    }

    public String buildCountSQL() {
        return dialect.countSQL(tableName, !whereClause.isEmpty()) +
                (whereClause.isEmpty() ? "" : " " + whereClause);
    }

    public Object[] getParameters() {
        return parameters.toArray();
    }
}