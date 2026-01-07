package io.safeaudit.core.processing.enrichment;

import io.safeaudit.core.domain.AuditContext;
import io.safeaudit.core.domain.AuditEvent;
import io.safeaudit.core.domain.enums.AuditSeverity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Nelson Tanko
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class CorrelationIdEnricherTest {

    private final CorrelationIdEnricher enricher = new CorrelationIdEnricher();

    @AfterEach
    void cleanup() {
        AuditContext.clear();
    }

    @Test
    void shouldEnrichEventWithCorrelationId() {
        // Given
        var correlationId = "correlation-123";
        AuditContext.set(AuditContext.CORRELATION_ID, correlationId);

        var event = createBaseEvent();

        // When
        var enriched = enricher.process(event);

        // Then
        assertThat(enriched.sessionId()).isEqualTo(correlationId);
    }

    @Test
    void shouldGenerateCorrelationIdWhenMissing() {
        // Given
        var event = createBaseEvent();

        // When
        var enriched = enricher.process(event);

        // Then
        assertThat(enriched.sessionId()).isNotNull();
        assertThat(enriched.sessionId()).matches("^[a-f0-9-]{36}$"); // UUID format
    }

    @Test
    void shouldNotOverrideExistingSessionId() {
        // Given
        var event = AuditEvent.builder()
                .eventId("test-123")
                .timestamp(Instant.now())
                .eventType("TEST")
                .severity(AuditSeverity.INFO)
                .sessionId("existing-session")
                .build();

        // When
        var enriched = enricher.process(event);

        // Then
        assertThat(enriched.sessionId()).isEqualTo("existing-session");
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

