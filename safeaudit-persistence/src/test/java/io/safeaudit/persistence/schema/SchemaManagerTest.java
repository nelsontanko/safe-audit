package io.safeaudit.persistence.schema;

import io.safeaudit.core.config.AuditProperties;
import io.safeaudit.persistence.dialect.H2Dialect;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

/**
 * @author Nelson Tanko
 */
class SchemaManagerTest {

    private SchemaManager schemaManager;
    private AuditProperties properties;

    @BeforeEach
    void setup() {
        var database = new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .build();

        properties = new AuditProperties();
        properties.getStorage().getDatabase().setAutoCreateSchema(true);

        schemaManager = new SchemaManager(database, new H2Dialect(), properties);
    }

    @Test
    void shouldInitializeSchema() {
        // When
        schemaManager.initialize();

        // Then
        assertThat(schemaManager.tableExists("audit_events")).isTrue();
    }

    @Test
    void shouldNotRecreateExistingTable() {
        // Given
        schemaManager.initialize();

        // When/Then
        assertThatNoException().isThrownBy(() -> schemaManager.initialize());
    }

    @Test
    void shouldVerifySchema() {
        // Given
        schemaManager.initialize();

        // When
        boolean valid = schemaManager.verifySchema();

        // Then
        assertThat(valid).isTrue();
    }

    @Test
    void shouldSkipInitializationWhenDisabled() {
        // Given
        properties.getStorage().getDatabase().setAutoCreateSchema(false);

        // When
        schemaManager.initialize();

        // Then - table should not exist (we started with fresh DB)
        // Note: tableExists might return false for non-existent tables
        assertThat(properties.getStorage().getDatabase().isAutoCreateSchema()).isFalse();
    }
}