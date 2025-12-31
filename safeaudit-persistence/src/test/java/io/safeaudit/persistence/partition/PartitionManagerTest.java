package io.safeaudit.persistence.partition;

import io.safeaudit.core.config.AuditProperties;
import io.safeaudit.persistence.dialect.SqlDialect;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PartitionManagerTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private SqlDialect dialect;

    @Mock
    private AuditProperties properties;

    private PartitionManager partitionManager;

    private AuditProperties.StorageConfig storageConfig;
    private AuditProperties.DatabaseConfig databaseConfig;
    private AuditProperties.PartitioningConfig partitioningConfig;

    @BeforeEach
    void setUp() {
        storageConfig = mock(AuditProperties.StorageConfig.class);
        databaseConfig = mock(AuditProperties.DatabaseConfig.class);
        partitioningConfig = new AuditProperties.PartitioningConfig();

        when(properties.getStorage()).thenReturn(storageConfig);
        when(storageConfig.getDatabase()).thenReturn(databaseConfig);
        when(databaseConfig.getPartitioning()).thenReturn(partitioningConfig);

        partitionManager = new PartitionManager(jdbcTemplate, dialect, properties);
    }

    @Test
    void shouldNotInitializeIfDisabled() {
        // Given
        partitioningConfig.setEnabled(false);

        // When
        partitionManager.initialize();

        // Then
        verify(dialect, never()).supportsPartitioning();
    }

    @Test
    void shouldNotInitializeIfDialectNotSupported() {
        // Given
        partitioningConfig.setEnabled(true);
        when(dialect.supportsPartitioning()).thenReturn(false);

        // When
        partitionManager.initialize();

        // Then
        verify(dialect).supportsPartitioning();
        verify(dialect, never()).createPartitionDDL(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void shouldInitializeAndCreateInitialPartitions() {
        // Given
        partitioningConfig.setEnabled(true);
        partitioningConfig.setAutoCreate(true);
        partitioningConfig.setStrategy(AuditProperties.PartitionStrategy.MONTHLY);

        when(dialect.supportsPartitioning()).thenReturn(true);
        when(dialect.getDatabaseType()).thenReturn("PostgreSQL");
        when(dialect.createPartitionDDL(anyString(), anyString(), anyString(), anyString()))
                .thenReturn("CREATE PARTITION...");

        // Mock partitionExists to return false (partition does not exist)
        when(jdbcTemplate.query(anyString(), any(PreparedStatementSetter.class), any(RowMapper.class)))
                .thenReturn(Collections.emptyList());

        // When
        partitionManager.initialize();

        // Then
        verify(dialect, atLeast(4))
                .createPartitionDDL(eq("audit_events"), anyString(), anyString(), anyString());
        verify(jdbcTemplate, atLeast(4)).execute(anyString());
    }

    @Test
    void shouldCreateFuturePartitionsWhenScheduled() {
        // Given
        partitioningConfig.setEnabled(true);
        partitioningConfig.setAutoCreate(true);
        partitioningConfig.setStrategy(AuditProperties.PartitionStrategy.DAILY);

        when(dialect.supportsPartitioning()).thenReturn(true);
        when(dialect.getDatabaseType()).thenReturn("MySQL");
        when(dialect.createPartitionDDL(anyString(), anyString(), anyString(), anyString()))
                .thenReturn("ALTER TABLE ADD PARTITION...");

        // Mock partitionExists check
        when(jdbcTemplate.query(anyString(), any(PreparedStatementSetter.class), any(RowMapper.class)))
                .thenReturn(Collections.emptyList());

        // When
        partitionManager.createFuturePartitions();

        // Then
        verify(dialect).createPartitionDDL(eq("audit_events"), anyString(), anyString(), anyString());
        verify(jdbcTemplate).execute(anyString());
    }

    @Test
    void shouldSkipPartitionCreationIfAlreadyExists() {
        // Given
        partitioningConfig.setStrategy(AuditProperties.PartitionStrategy.YEARLY);
        when(dialect.supportsPartitioning()).thenReturn(true);
        when(dialect.getDatabaseType()).thenReturn("PostgreSQL");

        // Mock partitionExists to return 1 (already exists)
        when(jdbcTemplate.query(anyString(), any(PreparedStatementSetter.class), any(RowMapper.class)))
                .thenReturn(List.of(1L));

        // When
        partitionManager.createPartition(LocalDate.now());

        // Then
        verify(dialect, never()).createPartitionDDL(anyString(), anyString(), anyString(), anyString());
        verify(jdbcTemplate, never()).execute(anyString());
    }

    @Test
    void shouldGenerateCorrectPartitionNamesAndRangesForDaily() {
        // Given
        partitioningConfig.setStrategy(AuditProperties.PartitionStrategy.DAILY);
        when(dialect.supportsPartitioning()).thenReturn(true);
        when(dialect.getDatabaseType()).thenReturn("PostgreSQL");
        when(jdbcTemplate.query(anyString(), any(PreparedStatementSetter.class), any(RowMapper.class)))
                .thenReturn(Collections.emptyList());

        var testDate = LocalDate.of(2025, 1, 1);
        var nameCaptor = ArgumentCaptor.forClass(String.class);
        var fromCaptor = ArgumentCaptor.forClass(String.class);
        var toCaptor = ArgumentCaptor.forClass(String.class);

        // When
        partitionManager.createPartition(testDate);

        // Then
        verify(dialect).createPartitionDDL(anyString(), nameCaptor.capture(), fromCaptor.capture(), toCaptor.capture());
        assertThat(nameCaptor.getValue()).isEqualTo("audit_events_20250101");
        assertThat(fromCaptor.getValue()).isEqualTo("2025-01-01");
        assertThat(toCaptor.getValue()).isEqualTo("2025-01-02");
    }

    @Test
    void shouldGenerateCorrectPartitionNamesAndRangesForMonthly() {
        // Given
        partitioningConfig.setStrategy(AuditProperties.PartitionStrategy.MONTHLY);
        when(dialect.supportsPartitioning()).thenReturn(true);
        when(dialect.getDatabaseType()).thenReturn("PostgreSQL");
        when(jdbcTemplate.query(anyString(), any(PreparedStatementSetter.class), any(RowMapper.class)))
                .thenReturn(Collections.emptyList());

        var testDate = LocalDate.of(2025, 1, 15);
        var nameCaptor = ArgumentCaptor.forClass(String.class);
        var fromCaptor = ArgumentCaptor.forClass(String.class);
        var toCaptor = ArgumentCaptor.forClass(String.class);

        // When
        partitionManager.createPartition(testDate);

        // Then
        verify(dialect).createPartitionDDL(anyString(), nameCaptor.capture(), fromCaptor.capture(), toCaptor.capture());
        assertThat(nameCaptor.getValue()).isEqualTo("audit_events_202501");
        assertThat(fromCaptor.getValue()).isEqualTo("2025-01-01");
        assertThat(toCaptor.getValue()).isEqualTo("2025-02-01");
    }

    @Test
    void shouldGenerateCorrectPartitionNamesAndRangesForYearly() {
        // Given
        partitioningConfig.setStrategy(AuditProperties.PartitionStrategy.YEARLY);
        when(dialect.supportsPartitioning()).thenReturn(true);
        when(dialect.getDatabaseType()).thenReturn("PostgreSQL");
        when(jdbcTemplate.query(anyString(), any(PreparedStatementSetter.class), any(RowMapper.class)))
                .thenReturn(Collections.emptyList());

        var testDate = LocalDate.of(2025, 6, 20);
        var nameCaptor = ArgumentCaptor.forClass(String.class);
        var fromCaptor = ArgumentCaptor.forClass(String.class);
        var toCaptor = ArgumentCaptor.forClass(String.class);

        // When
        partitionManager.createPartition(testDate);

        // Then
        verify(dialect).createPartitionDDL(anyString(), nameCaptor.capture(), fromCaptor.capture(), toCaptor.capture());
        assertThat(nameCaptor.getValue()).isEqualTo("audit_events_2025");
        assertThat(fromCaptor.getValue()).isEqualTo("2025-01-01");
        assertThat(toCaptor.getValue()).isEqualTo("2026-01-01");
    }
}
