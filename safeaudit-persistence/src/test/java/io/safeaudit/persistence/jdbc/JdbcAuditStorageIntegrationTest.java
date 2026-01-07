package io.safeaudit.persistence.jdbc;

import io.safeaudit.core.domain.AuditEvent;
import io.safeaudit.core.domain.ComplianceMetadata;
import io.safeaudit.core.domain.enums.AuditSeverity;
import io.safeaudit.core.spi.QueryCriteria;
import io.safeaudit.persistence.dialect.H2Dialect;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

/**
 * @author Nelson Tanko
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class JdbcAuditStorageIntegrationTest {

    private EmbeddedDatabase database;
    private JdbcAuditStorage storage;

    @BeforeEach
    void setup() {
        database = new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .build();

        storage = new JdbcAuditStorage(database, new H2Dialect());
        storage.initializeSchema();
    }

    @AfterEach
    void tearDown() {
        if (database != null) {
            database.shutdown();
        }
    }

    @Test
    void shouldStoreAndRetrieveAuditEvent() {
        // Given
        var event = createTestEvent(uuid("event-1"));

        // When
        boolean stored = storage.store(event);
        var retrieved = storage.findById(event.eventId());

        // Then
        assertThat(stored).isTrue();
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().eventId()).isEqualTo(event.eventId());
        assertThat(retrieved.get().eventType()).isEqualTo(event.eventType());
    }

    @Test
    void shouldHandleDuplicateEventIdempotently() {
        // Given
        var event = createTestEvent(uuid("duplicate-event"));

        // When
        boolean firstStore = storage.store(event);
        boolean secondStore = storage.store(event);

        // Then
        assertThat(firstStore).isTrue();
        assertThat(secondStore).isFalse();
    }

    @Test
    void shouldStoreBatchOfEvents() {
        // Given
        List<AuditEvent> events = List.of(
                createTestEvent(uuid("batch-1")),
                createTestEvent(uuid("batch-2")),
                createTestEvent(uuid("batch-3"))
        );

        // When
        int stored = storage.storeBatch(events);

        // Then
        assertThat(stored).isEqualTo(3);
    }

    @Test
    void shouldQueryEventsByUserId() {
        // Given
        storage.store(createTestEvent(uuid("event-1"), "user123"));
        storage.store(createTestEvent(uuid("event-2"), "user123"));
        storage.store(createTestEvent(uuid("event-3"), "user456"));

        var criteria = QueryCriteria.builder()
                .userId("user123")
                .build();

        // When
        var results = storage.query(criteria);

        // Then
        assertThat(results).hasSize(2)
                .allMatch(e -> e.userId().equals("user123"));
    }

    @Test
    void shouldQueryEventsWithPagination() {
        // Given
        for (int i = 0; i < 10; i++) {
            storage.store(createTestEvent(uuid("event-" + i)));
        }

        var criteria = QueryCriteria.builder()
                .page(0)
                .size(5)
                .build();

        // When
        var results = storage.query(criteria);

        // Then
        assertThat(results).hasSize(5);
    }

    @Test
    void shouldQueryEventsByDateRange() {
        // Given
        var now = Instant.now();
        var yesterday = now.minus(1, ChronoUnit.DAYS);
        var tomorrow = now.plus(1, ChronoUnit.DAYS);

        var oldEvent = AuditEvent.builder()
                .eventId(uuid("old-event"))
                .sequenceNumber(1L)
                .timestamp(yesterday.truncatedTo(ChronoUnit.MILLIS))
                .eventType("TEST")
                .eventHash("hash")
                .severity(AuditSeverity.INFO)
                .resource("resource1")
                .action("action1")
                .capturedBy("capturedBy1")
                .applicationName("applicationName1")
                .build();

        var event2 = uuid("recent-event");

        var recentEvent = createTestEvent(event2);

        storage.store(oldEvent);
        storage.store(recentEvent);

        var criteria = QueryCriteria.builder()
                .from(now.minus(1, ChronoUnit.HOURS))
                .to(tomorrow)
                .build();

        // When
        var results = storage.query(criteria);

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).eventId()).isEqualTo(event2);
    }

    @Test
    void shouldQueryEventsBySeverity() {
        // Given
        var infoEvent = AuditEvent.builder()
                .eventId(uuid("info-event")
                ).sequenceNumber(1L)
                .timestamp(Instant.now())
                .eventType("TEST")
                .eventHash("hash")
                .severity(AuditSeverity.INFO)
                .resource("resource1")
                .action("action1")
                .capturedBy("capturedBy1")
                .applicationName("applicationName1")
                .build();

        var criticalEvent = AuditEvent.builder()
                .eventId(uuid("critical-event"))
                .sequenceNumber(2L)
                .timestamp(Instant.now())
                .eventType("TEST")
                .eventHash("hash")
                .severity(AuditSeverity.CRITICAL)
                .resource("resource1")
                .action("action1")
                .capturedBy("capturedBy1")
                .applicationName("applicationName1")
                .build();

        storage.store(infoEvent);
        storage.store(criticalEvent);

        var criteria = QueryCriteria.builder()
                .severity(AuditSeverity.CRITICAL)
                .build();

        // When
        var results = storage.query(criteria);

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).severity()).isEqualTo(AuditSeverity.CRITICAL);
    }

    @Test
    void shouldCountEvents() {
        // Given
        for (int i = 0; i < 5; i++) {
            storage.store(createTestEvent(uuid("event-" + i), "user123"));
        }

        var criteria = QueryCriteria.builder()
                .userId("user123")
                .build();

        // When
        long count = storage.count(criteria);

        // Then
        assertThat(count).isEqualTo(5);
    }

    @Test
    void shouldVerifyIntegrity() {
        // Given
        var from = Instant.now();

        var event1 = AuditEvent.builder()
                .eventId(uuid("event-1"))
                .sequenceNumber(1L)
                .timestamp(from)
                .eventType("TEST")
                .severity(AuditSeverity.INFO)
                .eventHash("hash1")
                .action("action1")
                .resource("resource1")
                .capturedBy("capturedBy1")
                .applicationName("applicationName1")
                .previousEventHash(null)
                .build();

        var event2 = AuditEvent.builder()
                .eventId(uuid("event-2"))
                .sequenceNumber(2L)
                .timestamp(from.plusSeconds(1))
                .eventType("TEST2")
                .severity(AuditSeverity.INFO)
                .eventHash("hash2")
                .previousEventHash("hash1")
                .action("action2")
                .resource("resource2")
                .capturedBy("capturedBy2")
                .applicationName("applicationName2")
                .build();

        storage.store(event1);
        storage.store(event2);

        // Use a wide range to safely include the events
        var queryFrom = from.minusSeconds(1).truncatedTo(ChronoUnit.SECONDS);
        var to = Instant.now().plusSeconds(10);

        // When
        var report = storage.verifyIntegrity(queryFrom, to);

        // Then
        assertThat(report.valid()).isTrue();
        assertThat(report.totalEvents()).isEqualTo(2);
        assertThat(report.verifiedEvents()).isEqualTo(2);
    }

    @Test
    void shouldDetectIntegrityViolation() {
        // Given
        var from = Instant.now();

        var event1 = AuditEvent.builder()
                .eventId(uuid("event-1"))
                .sequenceNumber(1L)
                .timestamp(from)
                .eventType("TEST")
                .severity(AuditSeverity.INFO)
                .eventHash("hash1")
                .resource("resource1")
                .action("action1")
                .capturedBy("capturedBy1")
                .applicationName("applicationName1")
                .previousEventHash(null)
                .build();

        var event2 = AuditEvent.builder()
                .eventId(uuid("event-2"))
                .sequenceNumber(2L)
                .timestamp(from.plusSeconds(1))
                .eventType("TEST")
                .severity(AuditSeverity.INFO)
                .eventHash("hash2")
                .resource("resource")
                .action("action2")
                .capturedBy("capturedBy1")
                .applicationName("applicationName1")
                .previousEventHash("wrong-hash") // Broken chain!
                .build();

        storage.store(event1);
        storage.store(event2);

        var queryFrom = from.minusSeconds(1).truncatedTo(ChronoUnit.SECONDS);
        var to = Instant.now().plusSeconds(10);

        // When
        var report = storage.verifyIntegrity(queryFrom, to);

        // Then
        assertThat(report.valid()).isFalse();
        assertThat(report.violations()).isNotEmpty();
    }

    @Test
    void shouldInitializeSchema() {
        // When/Then
        assertThatNoException().isThrownBy(() -> storage.initializeSchema());

        // Verify table exists
        var template = new JdbcTemplate(database);
        var count = template.queryForObject(
                "SELECT COUNT(*) FROM audit_events WHERE 1=0",
                Long.class
        );
        assertThat(count).isZero();
    }

    private AuditEvent createTestEvent(String eventId) {
        return createTestEvent(eventId, "test-user");
    }

    private AuditEvent createTestEvent(String eventId, String userId) {
        return AuditEvent.builder()
                .eventId(eventId)
                .sequenceNumber(System.nanoTime())
                .timestamp(Instant.now().truncatedTo(ChronoUnit.MILLIS))
                .eventType("TEST_EVENT")
                .eventHash("hash1")
                .severity(AuditSeverity.INFO)
                .userId(userId)
                .username("Test User")
                .resource("/api/test")
                .action("GET")
                .capturedBy("capturedBy1")
                .applicationName("applicationName1")
                .compliance(ComplianceMetadata.empty())
                .build();
    }

    public String uuid(String name) {
        return UUID.nameUUIDFromBytes(name.getBytes()).toString();
    }
}
