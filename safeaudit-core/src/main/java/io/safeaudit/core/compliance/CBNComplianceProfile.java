package io.safeaudit.core.compliance;

import io.safeaudit.core.domain.AuditEvent;
import io.safeaudit.core.domain.ComplianceMetadata;
import io.safeaudit.core.domain.enums.DataClassification;
import io.safeaudit.core.exception.ComplianceViolationException;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Set;

import static io.safeaudit.core.compliance.NDPAComplianceProfile.getAuditEvent;

/**
 * Central Bank of Nigeria (CBN) compliance profile.
 * *
 * Requirements:
 * - 7-year retention minimum
 * - Tamper-evident logging
 * - Financial transaction tracking
 * - User authentication logging
 *
 * @author Nelson Tanko
 * @since 1.0.0
 */
public class CBNComplianceProfile implements ComplianceProfile {

    private static final Duration RETENTION_PERIOD = Duration.ofDays(2555); // 7 years
    private static final Set<String> REQUIRED_EVENTS = Set.of(
            "FINANCIAL_TRANSACTION",
            "USER_LOGIN",
            "USER_LOGOUT",
            "PERMISSION_CHANGE",
            "SYSTEM_ACCESS",
            "DATA_EXPORT"
    );

    private static final Set<String> SENSITIVE_FIELDS = Set.of(
            "accountNumber",
            "transactionAmount",
            "customerName",
            "bvn",
            "pin",
            "password"
    );

    @Override
    public String getRegulationCode() {
        return "CBN";
    }

    @Override
    public Set<String> getRequiredAuditEvents() {
        return REQUIRED_EVENTS;
    }

    @Override
    public Duration getRetentionPeriod() {
        return RETENTION_PERIOD;
    }

    @Override
    public boolean requiresTamperEvidence() {
        return true;
    }

    @Override
    public Set<String> getSensitiveFields() {
        return SENSITIVE_FIELDS;
    }

    @Override
    public AuditEvent enrich(AuditEvent event) {
        var enriched = ComplianceMetadata.builder()
                .addRegulatoryTag("CBN")
                .dataClassification(DataClassification.RESTRICTED)
                .retentionUntil(LocalDate.now().plusDays(RETENTION_PERIOD.toDays()))
                .containsPII(containsSensitiveData(event))
                .addProcessingPurpose("REGULATORY_COMPLIANCE")
                .addProcessingPurpose("FINANCIAL_AUDIT")
                .build();

        return getAuditEvent(event, enriched);
    }

    @Override
    public void validate(AuditEvent event) throws ComplianceViolationException {
        if (event.userId() == null) {
            throw new ComplianceViolationException(
                    "CBN",
                    "User ID is required for all audit events"
            );
        }

        if (requiresTamperEvidence() && event.eventHash() == null) {
            throw new ComplianceViolationException(
                    "CBN",
                    "Tamper-evident hashing is required"
            );
        }

        if (REQUIRED_EVENTS.contains(event.eventType()) && event.timestamp() == null) {
            throw new ComplianceViolationException(
                    "CBN",
                    "Timestamp is required for event type: " + event.eventType()
            );
        }
    }

    private boolean containsSensitiveData(AuditEvent event) {
        var payload = event.requestPayload();
        if (payload == null) {
            return false;
        }

        for (var field : SENSITIVE_FIELDS) {
            if (payload.contains(field)) {
                return true;
            }
        }
        return false;
    }
}
