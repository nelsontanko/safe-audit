package io.safeaudit.persistence;

import io.safeaudit.core.config.AuditProperties;
import io.safeaudit.core.exception.AuditConfigurationException;
import io.safeaudit.core.spi.AuditStorage;
import io.safeaudit.persistence.jdbc.JdbcAuditStorage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditStorageFactoryTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    @Mock
    private DatabaseMetaData metaData;

    @Mock
    private AuditProperties properties;

    @ParameterizedTest
    @ValueSource(strings = {"PostgreSQL", "MySQL", "H2"})
    void shouldCreateStorageForSupportedDatabases(String productName) throws SQLException {
        // Given
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getMetaData()).thenReturn(metaData);
        when(metaData.getDatabaseProductName()).thenReturn(productName);

        // When
        AuditStorage storage = AuditStorageFactory.create(dataSource, properties);

        // Then
        assertThat(storage).isInstanceOf(JdbcAuditStorage.class);
    }

    @Test
    void shouldThrowExceptionWhenConnectionFails() throws SQLException {
        // Given
        when(dataSource.getConnection()).thenThrow(new SQLException("Connection failed"));

        // When/Then
        assertThatThrownBy(() -> AuditStorageFactory.create(dataSource, properties))
                .isInstanceOf(AuditConfigurationException.class)
                .hasMessageContaining("Failed to detect database type");
    }

    @Test
    void shouldThrowExceptionForUnsupportedDatabase() throws SQLException {
        // Given
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getMetaData()).thenReturn(metaData);
        when(metaData.getDatabaseProductName()).thenReturn("UnknownDB");

        // When/Then
        assertThatThrownBy(() -> AuditStorageFactory.create(dataSource, properties))
                .isInstanceOf(AuditConfigurationException.class)
                .hasCauseInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void constructorShouldThrowException() throws NoSuchMethodException {
        Constructor<AuditStorageFactory> constructor = AuditStorageFactory.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        assertThatThrownBy(constructor::newInstance)
                .isInstanceOf(InvocationTargetException.class)
                .hasCauseInstanceOf(UnsupportedOperationException.class);
    }
}
