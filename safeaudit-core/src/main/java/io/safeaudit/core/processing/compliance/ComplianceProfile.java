package io.safeaudit.core.processing.compliance;

import io.safeaudit.core.domain.AuditEvent;
import io.safeaudit.core.exception.ComplianceViolationException;

import java.time.Duration;
import java.util.Set;

/**
 * SPI for regulatory compliance profiles.
 * Implementations define rules for specific regulations (CBN, NDPA, Tax).
 *
 * @author Nelson Tanko
 * @since 1.0.0
 */
public interface ComplianceProfile {

    /**
     * Get the regulatory code (e.g., "CBN", "NDPA", "TAX").
     */
    String getRegulationCode();

    /**
     * Get required event types that must be audited.
     */
    Set<String> getRequiredAuditEvents();

    /**
     * Get minimum data retention period.
     */
    Duration getRetentionPeriod();

    /**
     * Check if tamper-evident logging is required.
     */
    boolean requiresTamperEvidence();

    /**
     * Get fields containing PII that must be masked.
     */
    Set<String> getSensitiveFields();

    /**
     * Enrich event with compliance metadata.
     */
    AuditEvent enrich(AuditEvent event);

    /**
     * Validate event meets compliance requirements.
     *
     * @throws ComplianceViolationException if validation fails
     */
    void validate(AuditEvent event) throws ComplianceViolationException;
}
