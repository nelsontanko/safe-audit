package io.safeaudit.persistence.jdbc;

import java.util.Set;

/**
 * @author Nelson Tanko
 * @since 1.0.0
 */
public final class AuditColumnRegistry {

    private static final Set<String> ALLOWED_COLUMNS = Set.of(
            "event_id",
            "event_timestamp",
            "action",
            "entity_type",
            "entity_id",
            "username",
            "status",
            "severity"
    );

    private AuditColumnRegistry() {
    }

    public static String validate(String column) {
        if (!ALLOWED_COLUMNS.contains(column)) {
            throw new IllegalArgumentException("Illegal column: " + column);
        }
        return column;
    }
}
