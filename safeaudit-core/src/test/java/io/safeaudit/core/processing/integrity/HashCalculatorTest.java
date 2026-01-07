package io.safeaudit.core.processing.integrity;

import io.safeaudit.core.domain.AuditEvent;
import io.safeaudit.core.domain.enums.AuditSeverity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/**
 * @author Nelson Tanko
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class HashCalculatorTest {

    @Test
    void shouldCalculateEventHash() {
        // Given
        var calculator = new HashCalculator("SHA-256", false);
        var event = createBaseEvent();

        // When
        var hashed = calculator.process(event);

        // Then
        assertThat(hashed.eventHash()).isNotNull();
        assertThat(hashed.eventHash()).hasSize(44); // Base64 encoded SHA-256
    }

    @Test
    void shouldIncludePreviousHash() {
        // Given
        var calculator = new HashCalculator("SHA-256", true);

        // When
        var first = calculator.process(createBaseEvent());
        var second = calculator.process(createBaseEvent());

        // Then
        assertThat(first.previousEventHash()).isNull();
        assertThat(second.previousEventHash()).isEqualTo(first.eventHash());
    }

    @Test
    void shouldGenerateDifferentHashesForDifferentEvents() {
        // Given
        var calculator = new HashCalculator("SHA-256", false);

        var event1 = AuditEvent.builder()
                .eventId("event-1")
                .timestamp(Instant.now())
                .eventType("TEST")
                .severity(AuditSeverity.INFO)
                .build();

        var event2 = AuditEvent.builder()
                .eventId("event-2")
                .timestamp(Instant.now())
                .eventType("TEST")
                .severity(AuditSeverity.INFO)
                .build();

        // When
        var hashed1 = calculator.process(event1);
        var hashed2 = calculator.process(event2);

        // Then
        assertThat(hashed1.eventHash()).isNotEqualTo(hashed2.eventHash());
    }

    @Test
    void shouldHaveCorrectOrder() {
        // Given
        var calculator = new HashCalculator("SHA-256", false);

        // Then
        assertThat(calculator.getOrder()).isEqualTo(900);
    }

    @Test
    void shouldRejectInvalidAlgorithm() {
        // When/Then
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new HashCalculator("INVALID-ALGO", false))
                .withMessageContaining("not supported");
    }

    private AuditEvent createBaseEvent() {
        return AuditEvent.builder()
                .eventId("test-123")
                .timestamp(Instant.now())
                .eventType("TEST")
                .severity(AuditSeverity.INFO)
                .userId("user123")
                .resource("/api/test")
                .action("GET")
                .build();
    }
}
