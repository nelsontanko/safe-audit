package io.safeaudit.persistence.dialect;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Nelson Tanko
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class H2DialectTest {

    private final H2Dialect dialect = new H2Dialect();

    @Test
    void shouldReturnCorrectDatabaseType() {
        assertThat(dialect.getDatabaseType()).isEqualTo("H2");
    }

    @Test
    void shouldNotSupportPartitioning() {
        assertThat(dialect.supportsPartitioning()).isFalse();
    }

    @Test
    void shouldReturnCorrectDataTypes() {
        assertThat(dialect.getJsonType()).isEqualTo("VARCHAR(10000)");
        assertThat(dialect.getTimestampType()).isEqualTo("TIMESTAMP(6) WITH TIME ZONE");
        assertThat(dialect.getUuidType()).isEqualTo("UUID");
    }

    @Test
    void shouldGenerateCreateTableDDL() {
        // When
        String ddl = dialect.createTableDDL("audit_events");

        // Then
        assertThat(ddl).contains("CREATE TABLE IF NOT EXISTS audit_events")
                .contains("event_id UUID NOT NULL PRIMARY KEY")
                .doesNotContain("PARTITION");
    }

    @Test
    void shouldGenerateInsertSQLWithMerge() {
        // When
        var sql = dialect.insertSQL("audit_events");

        // Then
        assertThat(sql).contains("INSERT INTO audit_events")
                .contains("event_id");
    }

    @Test
    void shouldReturnEmptyPartitionDDL() {
        // When
        var ddl = dialect.createPartitionDDL("audit_events", "p1", "2025-01-01", "2025-02-01");

        // Then
        assertThat(ddl).isEmpty();
    }
}

