package io.safeaudit.persistence.jdbc;

import io.safeaudit.core.domain.AuditEvent;
import io.safeaudit.core.domain.HealthStatus;
import io.safeaudit.core.domain.enums.AuditSeverity;
import io.safeaudit.persistence.dialect.H2Dialect;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;

import javax.sql.DataSource;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * @author Nelson Tanko
 */
class JdbcAuditStorageTest {

    private JdbcTemplate jdbcTemplate;
    private JdbcAuditStorage storage;

    @BeforeEach
    void setup() {
        var dataSource = mock(DataSource.class);
        jdbcTemplate = mock(JdbcTemplate.class);
        storage = new JdbcAuditStorage(dataSource, new H2Dialect());

        // Inject mock JdbcTemplate using reflection
        try {
            var field = JdbcAuditStorage.class.getDeclaredField("jdbcTemplate");
            field.setAccessible(true);
            field.set(storage, jdbcTemplate);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void shouldStoreAuditEvent() {
        // Given
        when(jdbcTemplate.update(anyString(), any(PreparedStatementSetter.class))).thenReturn(1);
        var event = createTestEvent();

        // When
        boolean result = storage.store(event);

        // Then
        assertThat(result).isTrue();
        verify(jdbcTemplate).update(anyString(), any(PreparedStatementSetter.class));
    }

    @Test
    void shouldReturnFalseForDuplicateEvent() {
        // Given
        when(jdbcTemplate.update(anyString(), any(PreparedStatementSetter.class))).thenReturn(0);
        var event = createTestEvent();

        // When
        boolean result = storage.store(event);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void shouldStoreBatchOfEvents() {
        // Given
        when(jdbcTemplate.batchUpdate(anyString(), any(BatchPreparedStatementSetter.class)))
                .thenReturn(new int[]{1, 1, 1});
        List<AuditEvent> events = List.of(
                createTestEvent(),
                createTestEvent(),
                createTestEvent()
        );

        // When
        int result = storage.storeBatch(events);

        // Then
        assertThat(result).isEqualTo(3);
        verify(jdbcTemplate).batchUpdate(anyString(), any(BatchPreparedStatementSetter.class));
    }

    @Test
    void shouldReturnZeroForEmptyBatch() {
        // When
        int result = storage.storeBatch(List.of());

        // Then
        assertThat(result).isZero();
        verify(jdbcTemplate, never()).batchUpdate(anyString(), any(BatchPreparedStatementSetter.class));
    }

    @Test
    void shouldCheckHealthSuccessfully() {
        // Given
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class))).thenReturn(1);

        // When
        HealthStatus status = storage.checkHealth();

        // Then
        assertThat(status.isHealthy()).isTrue();
    }

    @Test
    void shouldReturnUnhealthyWhenCheckFails() {
        // Given
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class)))
                .thenThrow(new RuntimeException("Connection failed"));

        // When
        var status = storage.checkHealth();

        // Then
        assertThat(status.isHealthy()).isFalse();
        assertThat(status.getMessage()).contains("Connection failed");
    }

    private AuditEvent createTestEvent() {
        return AuditEvent.builder()
                .eventId("test-" + System.nanoTime())
                .sequenceNumber(1L)
                .timestamp(Instant.now())
                .eventType("TEST")
                .severity(AuditSeverity.INFO)
                .userId("user123")
                .resource("/api/test")
                .action("GET")
                .build();
    }
}
