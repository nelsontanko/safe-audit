package io.safeaudit.persistence.dialect;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Nelson Tanko
 */
class MySQLDialectTest {

    private final MySQLDialect dialect = new MySQLDialect();

    @Test
    void shouldReturnCorrectDatabaseType() {
        assertThat(dialect.getDatabaseType()).isEqualTo("MySQL");
    }

    @Test
    void shouldSupportPartitioning() {
        assertThat(dialect.supportsPartitioning()).isTrue();
    }

    @Test
    void shouldReturnCorrectDataTypes() {
        assertThat(dialect.getJsonType()).isEqualTo("JSON");
        assertThat(dialect.getTimestampType()).isEqualTo("TIMESTAMP(6)");
        assertThat(dialect.getUuidType()).isEqualTo("CHAR(36)");
    }

    @Test
    void shouldGenerateCreateTableDDL() {
        // When
        String ddl = dialect.createTableDDL("audit_events");

        // Then
        assertThat(ddl).contains("CREATE TABLE IF NOT EXISTS audit_events")
                .contains("event_id CHAR(36) NOT NULL")
                .contains("PARTITION BY RANGE COLUMNS(partition_key)");
    }

    @Test
    void shouldGenerateInsertSQLWithDuplicateKeyUpdate() {
        // When
        String sql = dialect.insertSQL("audit_events");

        // Then
        assertThat(sql).contains("INSERT INTO audit_events")
                .contains("ON DUPLICATE KEY UPDATE event_id = event_id");
    }

    @Test
    void shouldGeneratePartitionDDL() {
        // When
        var ddl = dialect.createPartitionDDL(
                "audit_events",
                "p_202501",
                "2025-01-01",
                "2025-02-01"
        );

        // Then
        assertThat(ddl).contains("ALTER TABLE audit_events ADD PARTITION")
                .contains("PARTITION p_202501")
                .contains("VALUES LESS THAN ('2025-02-01')");
    }
}
