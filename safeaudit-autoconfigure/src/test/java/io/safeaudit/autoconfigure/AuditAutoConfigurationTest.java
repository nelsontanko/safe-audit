package io.safeaudit.autoconfigure;

import io.micrometer.core.instrument.MeterRegistry;
import io.safeaudit.core.config.AuditProperties;
import io.safeaudit.core.processing.AuditProcessingPipeline;
import io.safeaudit.core.spi.AuditEventCapture;
import io.safeaudit.core.spi.AuditStorage;
import io.safeaudit.core.spi.LoggingAuditStorage;
import io.safeaudit.core.util.AuditEventIdGenerator;
import io.safeaudit.core.util.AuditMetrics;
import io.safeaudit.core.util.SequenceNumberGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * @author Nelson Tanko
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class AuditAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    AuditAutoConfiguration.class,
                    AuditStorageAutoConfiguration.class,
                    AuditProcessingAutoConfiguration.class,
                    AuditCaptureAutoConfiguration.class
            ));

    @Test
    void shouldRegisterCoreBeansByDefault() {
        contextRunner.withUserConfiguration(CoreConfiguration.class)
                .run(context -> {
                    assertThat(context).hasSingleBean(AuditProperties.class);
                    assertThat(context).hasSingleBean(AuditEventIdGenerator.class);
                    assertThat(context).hasSingleBean(SequenceNumberGenerator.class);
                    assertThat(context).hasSingleBean(AuditMetrics.class);
                });
    }

    @Test
    void shouldProvideMeterRegistryIfMissing() {
        contextRunner.withUserConfiguration(CoreConfiguration.class)
                .run(context -> {
                    assertThat(context).hasSingleBean(MeterRegistry.class);
                });
    }

    @Test
    void shouldLoadLoggingStorageWhenNoDataSourcePresent() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(AuditStorage.class);
            assertThat(context.getBean(AuditStorage.class)).isInstanceOf(LoggingAuditStorage.class);
            assertThat(context).hasSingleBean(AuditProcessingPipeline.class);
            assertThat(context).hasSingleBean(AuditEventCapture.class);
        });
    }

    @Configuration
    static class CoreConfiguration {
        @Bean
        AuditStorage auditStorage() {
            return mock(AuditStorage.class);
        }

        @Bean
        AuditProcessingPipeline auditProcessingPipeline() {
            return mock(AuditProcessingPipeline.class);
        }
    }
}
