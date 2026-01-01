package io.safeaudit.autoconfigure;

import io.safeaudit.core.processing.AsynchronousProcessingPipeline;
import io.safeaudit.core.processing.SynchronousProcessingPipeline;
import io.safeaudit.core.processing.compliance.PIIMasker;
import io.safeaudit.core.processing.enrichment.CorrelationIdEnricher;
import io.safeaudit.core.processing.enrichment.UserContextEnricher;
import io.safeaudit.core.processing.queue.VirtualThreadAuditQueue;
import io.safeaudit.core.spi.AuditStorage;
import io.safeaudit.persistence.schema.SchemaManager;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class AuditProcessingAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AuditProcessingAutoConfiguration.class, AuditAutoConfiguration.class));

    @Test
    void shouldCreateAsyncPipelineByDefault() {
        contextRunner.withUserConfiguration(StorageConfiguration.class)
                .run(context -> {
                    assertThat(context).hasSingleBean(AsynchronousProcessingPipeline.class);
                    assertThat(context).doesNotHaveBean(SynchronousProcessingPipeline.class);
                    assertThat(context).hasSingleBean(VirtualThreadAuditQueue.class);

                    // Default enrichers
                    assertThat(context).hasSingleBean(UserContextEnricher.class);
                    assertThat(context).hasSingleBean(CorrelationIdEnricher.class);
                });
    }

    @Test
    void shouldCreateSyncPipelineWhenConfigured() {
        contextRunner.withUserConfiguration(StorageConfiguration.class)
                .withPropertyValues("audit.processing.mode=SYNC")
                .run(context -> {
                    assertThat(context).hasSingleBean(SynchronousProcessingPipeline.class);
                    assertThat(context).doesNotHaveBean(AsynchronousProcessingPipeline.class);
                    assertThat(context).doesNotHaveBean(VirtualThreadAuditQueue.class);
                });
    }

    @Test
    void shouldCreatePIIMaskerWhenEnabled() {
        contextRunner.withUserConfiguration(StorageConfiguration.class)
                .withPropertyValues("audit.processing.compliance.pii-masking.enabled=true")
                .run(context -> {
                    assertThat(context).hasSingleBean(PIIMasker.class);
                });
    }

    @Configuration
    static class StorageConfiguration {
        @Bean
        public AuditStorage auditStorage() {
            return mock(AuditStorage.class);
        }

        @Bean
        public DataSource dataSource() {
            return mock(DataSource.class); // Required by AuditAutoConfiguration imports sometimes
        }

        @Bean
        public SchemaManager schemaManager() {
            return mock(SchemaManager.class);
        }
    }
}
