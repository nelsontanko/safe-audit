package io.safeaudit.persistence.retention;

import io.safeaudit.core.config.AuditProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * @author Nelson Tanko
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class RetentionPolicyTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private AuditProperties properties;

    private RetentionPolicy retentionPolicy;
    private AuditProperties.StorageConfig storageConfig;
    private AuditProperties.DatabaseConfig databaseConfig;
    private AuditProperties.RetentionConfig retentionConfig;

    @BeforeEach
    void setUp() {
        storageConfig = mock(AuditProperties.StorageConfig.class);
        databaseConfig = mock(AuditProperties.DatabaseConfig.class);
        retentionConfig = new AuditProperties.RetentionConfig();

        when(properties.getStorage()).thenReturn(storageConfig);
        when(storageConfig.getDatabase()).thenReturn(databaseConfig);
        when(databaseConfig.getRetention()).thenReturn(retentionConfig);

        retentionPolicy = new RetentionPolicy(jdbcTemplate, properties);
    }

    @Test
    void shouldNotArchiveIfDisabled() {
        // Given
        retentionConfig.setEnabled(false);

        // When
        retentionPolicy.archiveOldData();

        // Then
        verify(jdbcTemplate, never()).update(anyString(), any(LocalDate.class));
    }

    @Test
    void shouldNotArchiveIfArchivalDisabled() {
        // Given
        retentionConfig.setEnabled(true);
        retentionConfig.setArchivalEnabled(false);

        // When
        retentionPolicy.archiveOldData();

        // Then
        verify(jdbcTemplate, never()).update(anyString(), any(LocalDate.class));
    }

    @Test
    void shouldArchiveAndThenDeleteData() {
        // Given
        retentionConfig.setEnabled(true);
        retentionConfig.setArchivalEnabled(true);
        retentionConfig.setDefaultDays(30);

        // Mock insert into archive returns 5 rows
        when(jdbcTemplate.update(contains("INSERT INTO audit_events_archive"), any(LocalDate.class)))
                .thenReturn(5);

        // Mock delete from main table
        when(jdbcTemplate.update(contains("DELETE FROM audit_events"), any(LocalDate.class)))
                .thenReturn(5);

        // When
        retentionPolicy.archiveOldData();

        // Then
        verify(jdbcTemplate).update(contains("INSERT INTO audit_events_archive"), any(LocalDate.class));
        verify(jdbcTemplate).update(contains("DELETE FROM audit_events"), any(LocalDate.class));
    }

    @Test
    void shouldNotDeleteIfNoDataArchived() {
        // Given
        retentionConfig.setEnabled(true);
        retentionConfig.setArchivalEnabled(true);

        // Mock insert into archive returns 0 rows
        when(jdbcTemplate.update(contains("INSERT INTO audit_events_archive"), any(LocalDate.class)))
                .thenReturn(0);

        // When
        retentionPolicy.archiveOldData();

        // Then
        verify(jdbcTemplate).update(contains("INSERT INTO audit_events_archive"), any(LocalDate.class));
        verify(jdbcTemplate, never()).update(contains("DELETE FROM audit_events"), any(LocalDate.class));
    }

    @Test
    void shouldPurgeExpiredData() {
        // Given
        retentionConfig.setEnabled(true);
        retentionConfig.setDefaultDays(365);

        when(jdbcTemplate.update(anyString(), any(LocalDate.class))).thenReturn(10);

        // When
        int result = retentionPolicy.purgeExpiredData();

        // Then
        assertThat(result).isEqualTo(10);
        verify(jdbcTemplate).update(contains("DELETE FROM audit_events"), any(LocalDate.class));
    }

    @Test
    void shouldNotPurgeIfDisabled() {
        // Given
        retentionConfig.setEnabled(false);

        // When
        int result = retentionPolicy.purgeExpiredData();

        // Then
        assertThat(result).isZero();
        verify(jdbcTemplate, never()).update(anyString(), any(LocalDate.class));
    }
}
