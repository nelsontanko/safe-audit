package io.safeaudit.autoconfigure;

import io.safeaudit.core.spi.AuditEventCapture;
import io.safeaudit.core.spi.AuditStorage;
import io.safeaudit.persistence.jdbc.JdbcAuditStorage;
import io.safeaudit.persistence.partition.PartitionManager;
import io.safeaudit.persistence.retention.RetentionPolicy;
import io.safeaudit.persistence.schema.SchemaManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Nelson Tanko
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class AuditStorageAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AuditStorageAutoConfiguration.class, AuditAutoConfiguration.class));

    @Test
    void shouldCreateJdbcStorageWhenDataSourcePresent() {
        contextRunner.withUserConfiguration(DataSourceConfiguration.class)
                .run(context -> {
                    assertThat(context).hasSingleBean(AuditStorage.class);
                    assertThat(context).hasSingleBean(JdbcAuditStorage.class);
                    assertThat(context).hasSingleBean(SchemaManager.class); // SchemaManager is default
                });
    }

    @Test
    void shouldCreatePartitionManagerWhenEnabled() {
        contextRunner.withUserConfiguration(DataSourceConfiguration.class)
                .withPropertyValues("audit.storage.database.partitioning.enabled=true")
                .run(context -> {
                    assertThat(context).hasSingleBean(PartitionManager.class);
                });
    }

    @Test
    void shouldCreateRetentionPolicyWhenEnabled() {
        contextRunner.withUserConfiguration(DataSourceConfiguration.class)
                .withPropertyValues("audit.storage.database.retention.enabled=true")
                .run(context -> {
                    assertThat(context).hasSingleBean(RetentionPolicy.class);
                });
    }

    @Configuration
    static class DataSourceConfiguration {
        @Bean
        public DataSource dataSource() throws java.sql.SQLException {
            var dataSource = mock(DataSource.class);
            var connection = mock(java.sql.Connection.class);
            var metaData = mock(java.sql.DatabaseMetaData.class);

            when(dataSource.getConnection()).thenReturn(connection);
            when(connection.getMetaData()).thenReturn(metaData);
            when(metaData.getDatabaseProductName()).thenReturn("H2");

            return dataSource;
        }

        @Bean
        public AuditEventCapture auditEventCapture() {
            return mock(AuditEventCapture.class);
        }
    }
}
