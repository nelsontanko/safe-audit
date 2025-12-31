package io.safeaudit.core.processing.enrichment;

import io.safeaudit.core.domain.AuditContext;
import io.safeaudit.core.domain.AuditEvent;
import io.safeaudit.core.domain.enums.AuditSeverity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Nelson Tanko
 */
class UserContextEnricherTest {

    private final UserContextEnricher enricher = new UserContextEnricher();

    @AfterEach
    void cleanup() {
        AuditContext.clear();
    }

    @Test
    void shouldEnrichEventWithUserContext() {
        // Given
        AuditContext.set(AuditContext.USER_ID, "user123");
        AuditContext.set(AuditContext.USERNAME, "john.doe");

        AuditEvent event = createBaseEvent();

        // When
        AuditEvent enriched = enricher.process(event);

        // Then
        assertThat(enriched.userId()).isEqualTo("user123");
        assertThat(enriched.username()).isEqualTo("john.doe");
    }

    @Test
    void shouldNotOverrideExistingUserId() {
        // Given
        AuditContext.set(AuditContext.USER_ID, "context-user");

        AuditEvent event = AuditEvent.builder()
                .eventId("test-123")
                .timestamp(Instant.now())
                .eventType("TEST")
                .severity(AuditSeverity.INFO)
                .userId("existing-user")
                .build();

        // When
        var enriched = enricher.process(event);

        // Then
        assertThat(enriched.userId()).isEqualTo("existing-user");
    }

    @Test
    void shouldReturnOriginalEventWhenNoContext() {
        // Given
        var event = createBaseEvent();

        // When
        var enriched = enricher.process(event);

        // Then
        assertThat(enriched).isEqualTo(event);
    }

    @Test
    void shouldHaveCorrectOrder() {
        // Then
        assertThat(enricher.getOrder()).isEqualTo(100);
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