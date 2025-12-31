package io.safeaudit.core.compliance;

import io.safeaudit.core.domain.AuditEvent;
import io.safeaudit.core.domain.enums.AuditSeverity;
import io.safeaudit.core.domain.enums.DataClassification;
import io.safeaudit.core.exception.ComplianceViolationException;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

/**
 * @author Nelson Tanko
 */
class NDPAComplianceProfileTest {

    private final NDPAComplianceProfile profile = new NDPAComplianceProfile();

    @Test
    void shouldReturnNDPARegulationCode() {
        assertThat(profile.getRegulationCode()).isEqualTo("NDPA");
    }

    @Test
    void shouldRequireTamperEvidence() {
        assertThat(profile.requiresTamperEvidence()).isTrue();
    }

    @Test
    void shouldEnrichEventWithNDPATag() {
        // Given
        var event = createBaseEvent();

        // When
        var enriched = profile.enrich(event);

        // Then
        assertThat(enriched.compliance().regulatoryTags()).contains("NDPA");
    }

    @Test
    void shouldClassifyAsRestrictedWhenContainsPII() {
        // Given
        var event = AuditEvent.builder()
                .eventId("test-123")
                .timestamp(Instant.now())
                .eventType("TEST")
                .severity(AuditSeverity.INFO)
                .requestPayload("{\"email\":\"test@example.com\"}")
                .build();

        // When
        var enriched = profile.enrich(event);

        // Then
        assertThat(enriched.compliance().dataClassification())
                .isEqualTo(DataClassification.RESTRICTED);

    }

    @Test
    void shouldValidatePIIMasking() {
        // Given - Event with PII but not masked
        var event = AuditEvent.builder()
                .eventId("test-123")
                .timestamp(Instant.now())
                .eventType("TEST")
                .severity(AuditSeverity.INFO)
                .requestPayload("{\"email\":\"test@example.com\"}")
                .build();

        // When/Then
        assertThatExceptionOfType(ComplianceViolationException.class)
                .isThrownBy(() -> profile.validate(event))
                .withMessageContaining("PII must be masked");
    }

    @Test
    void shouldPassValidationForMaskedPII() {
        // Given - Event with masked PII
        var event = AuditEvent.builder()
                .eventId("test-123")
                .timestamp(Instant.now())
                .eventType("TEST")
                .severity(AuditSeverity.INFO)
                .requestPayload("{\"email\":\"SHA256:abc123...\"}")
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
                .build();
    }

}