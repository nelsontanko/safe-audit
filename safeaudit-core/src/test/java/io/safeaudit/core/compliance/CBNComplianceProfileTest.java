package io.safeaudit.core.compliance;

import io.safeaudit.core.domain.AuditEvent;
import io.safeaudit.core.domain.enums.AuditSeverity;
import io.safeaudit.core.domain.enums.DataClassification;
import io.safeaudit.core.exception.ComplianceViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

/**
 * @author Nelson Tanko
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class CBNComplianceProfileTest {

    private final CBNComplianceProfile profile = new CBNComplianceProfile();

    @Test
    void shouldReturnCBNRegulationCode() {
        assertThat(profile.getRegulationCode()).isEqualTo("CBN");
    }

    @Test
    void shouldRequire7YearRetention() {
        var retention = profile.getRetentionPeriod();
        assertThat(retention.toDays()).isEqualTo(2555); // 7 years
    }

    @Test
    void shouldRequireTamperEvidence() {
        assertThat(profile.requiresTamperEvidence()).isTrue();
    }

    @Test
    void shouldEnrichEventWithCBNTag() {
        // Given
        var event = createBaseEvent();

        // When
        var enriched = profile.enrich(event);

        // Then
        assertThat(enriched.compliance().regulatoryTags()).contains("CBN");
        assertThat(enriched.compliance().dataClassification())
                .isEqualTo(DataClassification.RESTRICTED);
        assertThat(enriched.compliance().retentionUntil()).isNotNull();
    }

    @Test
    void shouldValidateUserIdPresence() {
        // Given
        var event = AuditEvent.builder()
                .eventId("test-123")
                .timestamp(Instant.now())
                .eventType("TEST")
                .severity(AuditSeverity.INFO)
                .userId(null) // Missing
                .build();

        // When/Then
        assertThatExceptionOfType(ComplianceViolationException.class)
                .isThrownBy(() -> profile.validate(event))
                .withMessageContaining("User ID is required");
    }

    @Test
    void shouldValidateTamperEvidencePresence() {
        // Given
        var event = AuditEvent.builder()
                .eventId("test-123")
                .timestamp(Instant.now())
                .eventType("TEST")
                .severity(AuditSeverity.INFO)
                .userId("user123")
                .eventHash(null) // Missing
                .build();

        // When/Then
        assertThatExceptionOfType(ComplianceViolationException.class)
                .isThrownBy(() -> profile.validate(event))
                .withMessageContaining("Tamper-evident hashing is required");
    }

    @Test
    void shouldPassValidationForCompliantEvent() {
        // Given
        var event = AuditEvent.builder()
                .eventId("test-123")
                .timestamp(Instant.now())
                .eventType("TEST")
                .severity(AuditSeverity.INFO)
                .userId("user123")
                .eventHash("hash-value")
                .build();

        // When/Then
        assertThatNoException().isThrownBy(() -> profile.validate(event));
    }

    private AuditEvent createBaseEvent() {
        return AuditEvent.builder()
                .eventId("test-123")
                .timestamp(Instant.now())
                .eventType("TEST")
                .severity(AuditSeverity.INFO)
                .userId("user123")
                .build();
    }
}
