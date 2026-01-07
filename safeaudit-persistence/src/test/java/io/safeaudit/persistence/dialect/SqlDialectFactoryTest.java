package io.safeaudit.persistence.dialect;

import io.safeaudit.persistence.DatabaseType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * @author Nelson Tanko
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class SqlDialectFactoryTest {

    @Test
    void shouldCreatePostgreSQLDialect() {
        // When
        var dialect = SqlDialectFactory.create(DatabaseType.POSTGRESQL);

        // Then
        assertThat(dialect).isInstanceOf(PostgreSQLDialect.class);
        assertThat(dialect.getDatabaseType()).isEqualTo("PostgreSQL");
    }

    @Test
    void shouldCreateMySQLDialect() {
        // When
        var dialect = SqlDialectFactory.create(DatabaseType.MYSQL);

        // Then
        assertThat(dialect).isInstanceOf(MySQLDialect.class);
        assertThat(dialect.getDatabaseType()).isEqualTo("MySQL");
    }

    @Test
    void shouldCreateH2Dialect() {
        // When
        var dialect = SqlDialectFactory.create(DatabaseType.H2);

        // Then
        assertThat(dialect).isInstanceOf(H2Dialect.class);
        assertThat(dialect.getDatabaseType()).isEqualTo("H2");
    }

    @Test
    void shouldThrowExceptionForOracleNotImplemented() {
        // When/Then
        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() -> SqlDialectFactory.create(DatabaseType.ORACLE))
                .withMessageContaining("Oracle dialect not yet implemented");
    }

    @Test
    void shouldThrowExceptionForMSSQLNotImplemented() {
        // When/Then
        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() -> SqlDialectFactory.create(DatabaseType.MSSQL))
                .withMessageContaining("MSSQL dialect not yet implemented");
    }
}