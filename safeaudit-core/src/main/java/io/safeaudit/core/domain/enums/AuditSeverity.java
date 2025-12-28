package io.safeaudit.core.domain.enums;

/**
 * @author Nelson Tanko
 */
public enum AuditSeverity {
    /**
     * Informational events (e.g., user login, data view)
     */
    INFO,

    /**
     * Warning events (e.g., failed login attempt, validation error)
     */
    WARN,

    /**
     * Critical events (e.g., financial transaction, data modification, security breach)
     */
    CRITICAL
}