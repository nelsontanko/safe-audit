package io.safeaudit.persistence.dialect;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Nelson Tanko
 */
class PostgreSQLDialectTest {

    private final PostgreSQLDialect dialect = new PostgreSQLDialect();

    @Test
    void shouldReturnCorrectDatabaseType() {
        assertThat(dialect.getDatabaseType()).isEqualTo("PostgreSQL");
    }

    @Test
    void shouldSupportPartitioning() {
        assertThat(dialect.supportsPartitioning()).isTrue();
    }

    @Test
    void shouldReturnCorrectDataTypes() {
        assertThat(dialect.getJsonType()).isEqualTo("JSONB");
        assertThat(dialect.getTimestampType()).isEqualTo("TIMESTAMP(6) WITH TIME ZONE");
        assertThat(dialect.getUuidType()).isEqualTo("UUID");
    }

    @Test
    void shouldGenerateCreateTableDDL() {
        // When
        var ddl = dialect.createTableDDL("audit_events");

        // Then
        assertThat(ddl)
                .contains("CREATE TABLE IF NOT EXISTS audit_events")
                .contains("event_id UUID NOT NULL")
                .contains("PARTITION BY RANGE (partition_key)")
                .contains("CREATE INDEX");
    }

    @Test
    void shouldGenerateInsertSQL() {
        // When
        var sql = dialect.insertSQL("audit_events");

        // Then
        assertThat(sql)
                .contains("INSERT INTO audit_events")
                .contains("ON CONFLICT (event_id, partition_key) DO NOTHING");
    }

    @Test
    void shouldGenerateSelectByIdSQL() {
        // When
        var sql = dialect.selectByIdSQL("audit_events");

        // Then
        assertThat(sql).isEqualTo("SELECT * FROM audit_events WHERE event_id = ?");
    }

    @Test
    void shouldGenerateCountSQL() {
        // When
        var sqlWithoutWhere = dialect.countSQL("audit_events", false);
        var sqlWithWhere = dialect.countSQL("audit_events", true);

        // Then
        assertThat(sqlWithoutWhere).isEqualTo("SELECT COUNT(*) FROM audit_events ");
        assertThat(sqlWithWhere).isEqualTo("SELECT COUNT(*) FROM audit_events WHERE");
    }

    @Test
    void shouldGenerateSelectSQLWithPagination() {
        // When
        var sql = dialect.selectSQL(
                "audit_events",
                "user_id = ?",
                "timestamp DESC",
                50,
                100
        );

        // Then
        assertThat(sql).contains("SELECT * FROM audit_events")
                .contains("WHERE user_id = ?")
                .contains("ORDER BY timestamp DESC")
                .contains("LIMIT 50")
                .contains("OFFSET 100");
    }

    @Test
    void shouldGeneratePartitionDDL() {
        // When
        var ddl = dialect.createPartitionDDL(
                "audit_events",
                "audit_events_202501",
                "2025-01-01",
                "2025-02-01"
        );

        // Then
        assertThat(ddl).contains("CREATE TABLE IF NOT EXISTS audit_events_202501")
                .contains("PARTITION OF audit_events")
                .contains("FOR VALUES FROM ('2025-01-01') TO ('2025-02-01')");
    }
}