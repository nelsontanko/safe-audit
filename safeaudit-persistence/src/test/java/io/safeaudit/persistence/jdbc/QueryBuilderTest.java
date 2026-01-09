package io.safeaudit.persistence.jdbc;

import io.safeaudit.core.domain.QueryCriteria;
import io.safeaudit.core.domain.enums.AuditSeverity;
import io.safeaudit.persistence.dialect.PostgreSQLDialect;
import io.safeaudit.persistence.dialect.SqlDialect;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Nelson Tanko
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class QueryBuilderTest {

    private SqlDialect dialect;
    private final String tableName = "audit_events";

    @BeforeEach
    void setUp() {
        dialect = new PostgreSQLDialect();
    }

    @Test
    void shouldBuildBasicSelectSQL() {
        // Given
        QueryCriteria criteria = QueryCriteria.builder().build();
        QueryBuilder builder = new QueryBuilder(criteria, dialect, tableName);

        // When
        String sql = builder.buildSelectSQL();

        // Then
        assertThat(sql).contains("SELECT * FROM audit_events");
//        assertThat(sql).contains("ORDER BY event_timestamp ASC");
        assertThat(sql).contains("LIMIT 50");
    }

    @Test
    void shouldBuildSelectSQLWithFilters() {
        // Given
        Instant now = Instant.now();
        QueryCriteria criteria = QueryCriteria.builder()
                .userId("user1")
                .eventType("LOGIN")
                .severity(AuditSeverity.CRITICAL)
                .from(now)
                .build();
        QueryBuilder builder = new QueryBuilder(criteria, dialect, tableName);

        // When
        String sql = builder.buildSelectSQL();
        Object[] params = builder.getParameters();

        // Then
        assertThat(sql).contains("WHERE user_id = ? AND event_type = ? AND event_timestamp >= ?");
        assertThat(params).hasSize(4);
        assertThat(params[0]).isEqualTo("user1");
        assertThat(params[1]).isEqualTo("LOGIN");
//        assertThat(params[2]).isEqualTo("CRITICAL");
    }

    @Test
    void shouldBuildSelectSQLWithLikeFilters() {
        // Given
        QueryCriteria criteria = QueryCriteria.builder()
                .username("john")
                .resource("payment")
                .build();
        QueryBuilder builder = new QueryBuilder(criteria, dialect, tableName);

        // When
        String sql = builder.buildSelectSQL();
        Object[] params = builder.getParameters();

        // Then
        assertThat(sql).contains("WHERE username LIKE ? AND resource LIKE ?");
        assertThat(params).hasSize(2);
        assertThat(params[0]).isEqualTo("%john%");
        assertThat(params[1]).isEqualTo("%payment%");
    }

    @Test
    void shouldBuildSelectSQLWithMultipleSeverities() {
        // Given
        QueryCriteria criteria = QueryCriteria.builder()
                .severities(Set.of(AuditSeverity.CRITICAL, AuditSeverity.WARN))
                .build();
        QueryBuilder builder = new QueryBuilder(criteria, dialect, tableName);

        // When
        String sql = builder.buildSelectSQL();
        Object[] params = builder.getParameters();

        // Then
        assertThat(sql).contains("WHERE severity IN (?,?)");
        assertThat(params).hasSize(2);
        assertThat(params).containsExactlyInAnyOrder("CRITICAL", "WARN");
    }

    @Test
    void shouldBuildSelectSQLWithCustomSorting() {
        // Given
        QueryCriteria criteria = QueryCriteria.builder()
                .sortBy("user_id")
                .sortDirection(QueryCriteria.SortDirection.DESC)
                .build();
        QueryBuilder builder = new QueryBuilder(criteria, dialect, tableName);

        // When
        String sql = builder.buildSelectSQL();

        // Then
        assertThat(sql).contains("ORDER BY user_id DESC");
    }

    @Test
    void shouldThrowExceptionForInvalidSortColumn() {
        // Given
        QueryCriteria criteria = QueryCriteria.builder()
                .sortBy("invalid_column")
                .build();
        QueryBuilder builder = new QueryBuilder(criteria, dialect, tableName);

        // When/Then
        assertThatThrownBy(builder::buildSelectSQL)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid sort column");
    }

    @Test
    void shouldBuildCountSQLWithoutWhere() {
        // Given
        QueryCriteria criteria = QueryCriteria.builder().build();
        QueryBuilder builder = new QueryBuilder(criteria, dialect, tableName);

        // When
        String sql = builder.buildCountSQL();

        // Then
        assertThat(sql).isEqualTo("SELECT COUNT(*) FROM audit_events ");
    }

    @Test
    void shouldBuildCountSQLWithWhere() {
        // Given
        QueryCriteria criteria = QueryCriteria.builder()
                .userId("user1")
                .build();
        QueryBuilder builder = new QueryBuilder(criteria, dialect, tableName);

        // When
        String sql = builder.buildCountSQL();

        // Then
        assertThat(sql).isEqualTo("SELECT COUNT(*) FROM audit_events WHERE user_id = ?");
    }

    @Test
    void shouldHandlePaginationCorrectly() {
        // Given
        QueryCriteria criteria = QueryCriteria.builder()
                .page(2)
                .size(50)
                .build();
        QueryBuilder builder = new QueryBuilder(criteria, dialect, tableName);

        // When
        String sql = builder.buildSelectSQL();

        // Then
        assertThat(sql).contains("LIMIT 50 OFFSET 100");
    }
}
