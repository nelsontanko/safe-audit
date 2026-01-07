package io.safeaudit.core.processing.compliance;

import io.safeaudit.core.config.AuditProperties;
import io.safeaudit.core.domain.AuditEvent;
import io.safeaudit.core.domain.enums.AuditSeverity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Nelson Tanko
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class PIIMaskerTest {

    @Test
    void shouldMaskPIIFieldsWithHashStrategy() {
        // Given
        var masker = new PIIMasker(
                Set.of("email", "phoneNumber"),
                AuditProperties.PIIMaskingStrategy.HASH
        );

        var payload = "{\"email\":\"user@example.com\",\"name\":\"John\"}";
        var event = createEventWithPayload(payload);

        // When
        var masked = masker.process(event);

        // Then
        assertThat(masked.requestPayload()).contains("SHA256:");
        assertThat(masked.requestPayload()).doesNotContain("user@example.com");
    }

    @Test
    void shouldMaskPIIFieldsWithMaskStrategy() {
        // Given
        var masker = new PIIMasker(
                Set.of("email"),
                AuditProperties.PIIMaskingStrategy.MASK
        );

        var payload = "{\"email\":\"test@example.com\"}";
        var event = createEventWithPayload(payload);

        // When
        var masked = masker.process(event);

        // Then
        assertThat(masked.requestPayload()).contains("****");
        assertThat(masked.requestPayload()).doesNotContain("test@example.com");
    }

    @Test
    void shouldRedactPIIFieldsWithRedactStrategy() {
        // Given
        var masker = new PIIMasker(
                Set.of("email"),
                AuditProperties.PIIMaskingStrategy.REDACT
        );

        var payload = "{\"email\":\"test@example.com\"}";
        var event = createEventWithPayload(payload);

        // When
        var masked = masker.process(event);

        // Then
        assertThat(masked.requestPayload()).contains("[REDACTED]");
        assertThat(masked.requestPayload()).doesNotContain("test@example.com");
    }

    @Test
    void shouldNotMaskNonPIIFields() {
        // Given
        var masker = new PIIMasker(
                Set.of("email"),
                AuditProperties.PIIMaskingStrategy.HASH
        );

        var payload = "{\"name\":\"John\",\"age\":30}";
        var event = createEventWithPayload(payload);

        // When
        var masked = masker.process(event);

        // Then
        assertThat(masked.requestPayload()).contains("John");
        assertThat(masked.requestPayload()).contains("30");
    }

    @Test
    void shouldHandleNullPayload() {
        // Given
        var masker = new PIIMasker(
                Set.of("email"),
                AuditProperties.PIIMaskingStrategy.HASH
        );

        var event = createEventWithPayload(null);

        // When
        var masked = masker.process(event);

        // Then
        assertThat(masked.requestPayload()).isNull();
    }

    @Test
    void shouldHandleInvalidJSON() {
        // Given
        var masker = new PIIMasker(
                Set.of("email"),
                AuditProperties.PIIMaskingStrategy.HASH
        );

        var event = createEventWithPayload("not-json-data");

        // When
        var masked = masker.process(event);

        // Then
        assertThat(masked.requestPayload()).isEqualTo("not-json-data");
    }

    @Test
    void shouldHaveCorrectOrder() {
        // Given
        var masker = new PIIMasker(Set.of(), AuditProperties.PIIMaskingStrategy.HASH);

        // Then
        assertThat(masker.getOrder()).isEqualTo(300);
    }

    private AuditEvent createEventWithPayload(String payload) {
        return AuditEvent.builder()
                .eventId("test-123")
                .timestamp(Instant.now())
                .eventType("TEST")
                .severity(AuditSeverity.INFO)
                .requestPayload(payload)
                .build();
    }
}