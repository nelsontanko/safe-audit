package io.safeaudit.core.domain;

import io.safeaudit.core.domain.enums.AuditSeverity;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

/**
 * @author Nelson Tanko
 */
class AuditEventTest {

    @Test
    void shouldCreateValidAuditEvent() {
        // Given
        var eventId = "test-event-123";
        var timestamp = Instant.now();

        // When
        var event = AuditEvent.builder()
                .eventId(eventId)
                .sequenceNumber(1L)
                .timestamp(timestamp)
                .eventType("TEST_EVENT")
                .severity(AuditSeverity.INFO)
                .userId("user123")
                .resource("/api/test")
                .action("GET")
                .build();

        // Then
        assertThat(event.eventId()).isEqualTo(eventId);
        assertThat(event.timestamp()).isEqualTo(timestamp);
        assertThat(event.eventType()).isEqualTo("TEST_EVENT");
        assertThat(event.severity()).isEqualTo(AuditSeverity.INFO);
    }

    @Test
    void shouldRejectNullEventId() {
        // When/Then
        assertThatNullPointerException()
                .isThrownBy(() -> AuditEvent.builder()
                        .eventId(null)
                        .timestamp(Instant.now())
                        .eventType("TEST")
                        .severity(AuditSeverity.INFO)
                        .build())
                .withMessageContaining("eventId is required");
    }

    @Test
    void shouldRejectNullTimestamp() {
        // When/Then
        assertThatNullPointerException()
                .isThrownBy(() -> AuditEvent.builder()
                        .eventId("test-123")
                        .timestamp(null)
                        .eventType("TEST")
                        .severity(AuditSeverity.INFO)
                        .build())
                .withMessageContaining("timestamp is required");
    }

    @Test
    void shouldRejectNullEventType() {
        // When/Then
        assertThatNullPointerException()
                .isThrownBy(() -> AuditEvent.builder()
                        .eventId("test-123")
                        .timestamp(Instant.now())
                        .eventType(null)
                        .severity(AuditSeverity.INFO)
                        .build())
                .withMessageContaining("eventType is required");
    }

    @Test
    void shouldRejectNullSeverity() {
        // When/Then
        assertThatNullPointerException()
                .isThrownBy(() -> AuditEvent.builder()
                        .eventId("test-123")
                        .timestamp(Instant.now())
                        .eventType("TEST")
                        .severity(null)
                        .build())
                .withMessageContaining("severity is required");
    }

    @Test
    void shouldSetDefaultComplianceMetadata() {
        // When
        AuditEvent event = AuditEvent.builder()
                .eventId("test-123")
                .timestamp(Instant.now())
                .eventType("TEST")
                .severity(AuditSeverity.INFO)
                .build();

        // Then
        assertThat(event.compliance()).isNotNull();
    }

    @Test
    void shouldCompareEventsByEventId() {
        // Given
        var eventId = "test-123";
        var event1 = AuditEvent.builder()
                .eventId(eventId)
                .timestamp(Instant.now())
                .eventType("TEST")
                .severity(AuditSeverity.INFO)
                .build();

        var event2 = AuditEvent.builder()
                .eventId(eventId)
                .timestamp(Instant.now().plusSeconds(60))
                .eventType("OTHER")
                .severity(AuditSeverity.WARN)
                .build();

        // Then
        assertThat(event1).isEqualTo(event2);
        assertThat(event1.hashCode()).hasSameHashCodeAs(event2.hashCode());
    }

    @Test
    void shouldIncludeAllFieldsInBuilder() {
        // Given
        var compliance = ComplianceMetadata.builder()
                .addRegulatoryTag("CBN")
                .build();

        // When
        var event = AuditEvent.builder()
                .eventId("test-123")
                .sequenceNumber(42L)
                .timestamp(Instant.now())
                .eventType("TEST")
                .severity(AuditSeverity.CRITICAL)
                .userId("user123")
                .username("john.doe")
                .ipAddress("192.168.1.1")
                .userAgent("Mozilla/5.0")
                .resource("/api/accounts")
                .action("POST")
                .sessionId("session-456")
                .tenantId("tenant-789")
                .requestPayload("{\"test\":\"data\"}")
                .responsePayload("{\"result\":\"ok\"}")
                .httpStatusCode(200)
                .compliance(compliance)
                .previousEventHash("prev-hash")
                .eventHash("current-hash")
                .capturedBy("framework-1.0")
                .applicationName("test-app")
                .applicationInstance("server-01")
                .build();

        // Then
        assertThat(event.eventId()).isEqualTo("test-123");
        assertThat(event.sequenceNumber()).isEqualTo(42L);
        assertThat(event.userId()).isEqualTo("user123");
        assertThat(event.username()).isEqualTo("john.doe");
        assertThat(event.ipAddress()).isEqualTo("192.168.1.1");
        assertThat(event.compliance()).isEqualTo(compliance);
    }
}
