package io.safeaudit.persistence;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/**
 * @author Nelson Tanko
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class DatabaseTypeTest {

    @Test
    void shouldDetectPostgreSQLFromProductName() {
        // When
        var type = DatabaseType.fromProductName("PostgreSQL");

        // Then
        assertThat(type).isEqualTo(DatabaseType.POSTGRESQL);
    }

    @Test
    void shouldDetectPostgresFromProductName() {
        // When
        var type = DatabaseType.fromProductName("postgres");

        // Then
        assertThat(type).isEqualTo(DatabaseType.POSTGRESQL);
    }

    @Test
    void shouldDetectMySQLFromProductName() {
        // When
        var type = DatabaseType.fromProductName("MySQL");

        // Then
        assertThat(type).isEqualTo(DatabaseType.MYSQL);
    }

    @Test
    void shouldDetectMariaDBAsMySQL() {
        // When
        var type = DatabaseType.fromProductName("MariaDB");

        // Then
        assertThat(type).isEqualTo(DatabaseType.MYSQL);
    }

    @Test
    void shouldDetectOracleFromProductName() {
        // When
        var type = DatabaseType.fromProductName("Oracle Database");

        // Then
        assertThat(type).isEqualTo(DatabaseType.ORACLE);
    }

    @Test
    void shouldDetectMSSQLFromProductName() {
        // When
        var type = DatabaseType.fromProductName("Microsoft SQL Server");

        // Then
        assertThat(type).isEqualTo(DatabaseType.MSSQL);
    }

    @Test
    void shouldDetectH2FromProductName() {
        // When
        var type = DatabaseType.fromProductName("H2");

        // Then
        assertThat(type).isEqualTo(DatabaseType.H2);
    }

    @Test
    void shouldDefaultToH2ForNullProductName() {
        // When
        var type = DatabaseType.fromProductName(null);

        // Then
        assertThat(type).isEqualTo(DatabaseType.H2);
    }

    @Test
    void shouldThrowExceptionForUnsupportedDatabase() {
        // When/Then
        assertThatIllegalArgumentException()
                .isThrownBy(() -> DatabaseType.fromProductName("UnsupportedDB"))
                .withMessageContaining("Unsupported database");
    }

    @Test
    void shouldBeCaseInsensitive() {
        // When
        var postgres = DatabaseType.fromProductName("POSTGRESQL");
        var mysql = DatabaseType.fromProductName("mysql");

        // Then
        assertThat(postgres).isEqualTo(DatabaseType.POSTGRESQL);
        assertThat(mysql).isEqualTo(DatabaseType.MYSQL);
    }

    @Test
    void shouldHaveCorrectDriverClassNames() {
        assertThat(DatabaseType.POSTGRESQL.getDriverClassName())
                .isEqualTo("org.postgresql.Driver");
        assertThat(DatabaseType.MYSQL.getDriverClassName())
                .isEqualTo("com.mysql.cj.jdbc.Driver");
        assertThat(DatabaseType.H2.getDriverClassName())
                .isEqualTo("org.h2.Driver");
    }
}

