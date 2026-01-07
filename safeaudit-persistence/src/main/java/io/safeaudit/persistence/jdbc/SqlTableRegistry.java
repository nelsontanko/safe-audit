package io.safeaudit.persistence.jdbc;

import java.util.Set;

/**
 * @author Nelson Tanko
 * @since 1.0.0
 */
public final class SqlTableRegistry {

    private static final Set<String> ALLOWED_TABLES = Set.of(
            "audit_events",
            "audit_logs",
            "ndpa_audit_logs",
            "cbn_transaction_audits"
    );

    private SqlTableRegistry() {
    }

    public static String resolve(String tableName) {
        if (!ALLOWED_TABLES.contains(tableName)) {
            throw new IllegalArgumentException("Illegal table name: " + tableName);
        }
        return tableName;
    }
}
