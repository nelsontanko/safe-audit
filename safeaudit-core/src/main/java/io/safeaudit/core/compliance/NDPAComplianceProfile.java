package io.safeaudit.core.compliance;

import io.safeaudit.core.domain.AuditEvent;
import io.safeaudit.core.domain.ComplianceMetadata;
import io.safeaudit.core.domain.enums.DataClassification;
import io.safeaudit.core.exception.ComplianceViolationException;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Set;

/**
 * Nigeria Data Protection Act (NDPA) compliance profile.
 * Requirements:
 * - PII identification and masking
 * - Consent tracking
 * - Data breach logging
 * - Right to erasure support (audit exception)
 *
 * @author Nelson Tanko
 */
public class NDPAComplianceProfile implements ComplianceProfile {

    private static final Duration RETENTION_PERIOD = Duration.ofDays(365 * 6); // 6 years
    private static final Set<String> REQUIRED_EVENTS = Set.of(
            "DATA_ACCESS",
            "DATA_MODIFICATION",
            "DATA_DELETION",
            "CONSENT_GIVEN",
            "CONSENT_WITHDRAWN",
            "DATA_BREACH"
    );

    private static final Set<String> PII_FIELDS = Set.of(
            "email",
            "phoneNumber",
            "bvn",
            "nin",
            "address",
            "dateOfBirth",
            "passport",
            "firstName",
            "lastName"
    );

    @Override
    public String getRegulationCode() {
        return "NDPA";
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
        return PII_FIELDS;
    }

    @Override
    public AuditEvent enrich(AuditEvent event) {
        var enriched = ComplianceMetadata.builder()
                .addRegulatoryTag("NDPA")
                .dataClassification(determineClassification(event))
                .retentionUntil(LocalDate.now().plusDays(RETENTION_PERIOD.toDays()))
                .containsPII(containsPII(event))
                .addProcessingPurpose("DATA_PROTECTION")
                .addProcessingPurpose("LEGAL_COMPLIANCE")
                .build();

        return getAuditEvent(event, enriched);
    }

    static AuditEvent getAuditEvent(AuditEvent event, ComplianceMetadata enriched) {
        return AuditEvent.builder()
                .eventId(event.eventId())
                .sequenceNumber(event.sequenceNumber())
                .timestamp(event.timestamp())
                .eventType(event.eventType())
                .severity(event.severity())
                .userId(event.userId())
                .username(event.username())
                .ipAddress(event.ipAddress())
                .userAgent(event.userAgent())
                .resource(event.resource())
                .action(event.action())
                .sessionId(event.sessionId())
                .tenantId(event.tenantId())
                .requestPayload(event.requestPayload())
                .responsePayload(event.responsePayload())
                .httpStatusCode(event.httpStatusCode())
                .compliance(enriched)
                .previousEventHash(event.previousEventHash())
                .eventHash(event.eventHash())
                .capturedBy(event.capturedBy())
                .applicationName(event.applicationName())
                .applicationInstance(event.applicationInstance())
                .build();
    }

    @Override
    public void validate(AuditEvent event) throws ComplianceViolationException {
        if (containsPII(event) && !isPIIMasked(event)) {
            throw new ComplianceViolationException(
                    "NDPA",
                    "PII must be masked in audit logs"
            );
        }
    }

    private boolean containsPII(AuditEvent event) {
        var payload = event.requestPayload();
        if (payload == null) {
            return false;
        }

        for (var field : PII_FIELDS) {
            if (payload.contains(field)) {
                return true;
            }
        }
        return false;
    }

    private boolean isPIIMasked(AuditEvent event) {
        var payload = event.requestPayload();
        if (payload == null) {
            return true;
        }
        // Check for hash markers
        return payload.contains("SHA256:") || payload.contains("[REDACTED]");
    }

    private DataClassification determineClassification(AuditEvent event) {
        if (containsPII(event)) {
            return DataClassification.RESTRICTED;
        }
        return DataClassification.CONFIDENTIAL;
    }
}