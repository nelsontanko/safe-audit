package io.safeaudit.core.domain.enums;

/**
 * @author Nelson Tanko
 */
public enum DataClassification {
    /**
     * Public data - no restrictions
     */
    PUBLIC,

    /**
     * Internal data - normal business operations
     */
    INTERNAL,

    /**
     * Confidential data - restricted access
     */
    CONFIDENTIAL,

    /**
     * Restricted data - highest security, PII, financial data
     */
    RESTRICTED
}
