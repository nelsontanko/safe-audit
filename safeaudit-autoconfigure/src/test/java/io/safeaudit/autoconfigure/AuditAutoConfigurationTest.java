package io.safeaudit.autoconfigure;

import io.micrometer.core.instrument.MeterRegistry;
import io.safeaudit.core.config.AuditProperties;
import io.safeaudit.core.processing.AuditProcessingPipeline;
import io.safeaudit.core.spi.AuditStorage;
import io.safeaudit.core.util.AuditEventIdGenerator;
import io.safeaudit.core.util.AuditMetrics;
import io.safeaudit.core.util.SequenceNumberGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class AuditAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AuditAutoConfiguration.class));

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
    void shouldNotRegisterBeansWhenDisabled() {
        contextRunner
                .withUserConfiguration(CoreConfiguration.class)
                .withPropertyValues("audit.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(AuditProperties.class);
                    assertThat(context).doesNotHaveBean(AuditEventIdGenerator.class);
                    assertThat(context).doesNotHaveBean(SequenceNumberGenerator.class);
                    assertThat(context).doesNotHaveBean(AuditMetrics.class);
                });
    }

    @Test
    void shouldProvideMeterRegistryIfMissing() {
        contextRunner.withUserConfiguration(CoreConfiguration.class)
                .run(context -> {
                    assertThat(context).hasSingleBean(MeterRegistry.class);
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
