package io.safeaudit.autoconfigure;

import io.safeaudit.core.processing.AuditProcessingPipeline;
import io.safeaudit.core.spi.AuditEventCapture;
import io.safeaudit.web.capture.AuditHttpFilter;
import io.safeaudit.web.capture.AuditMethodInterceptor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class AuditCaptureAutoConfigurationTest {

    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AuditCaptureAutoConfiguration.class, AuditAutoConfiguration.class));

    @Test
    void shouldRegisterCaptureBeansByDefault() {
        contextRunner.withUserConfiguration(PipelineConfiguration.class)
                .run(context -> {
                    assertThat(context).hasSingleBean(AuditEventCapture.class);
                    assertThat(context).hasSingleBean(FilterRegistrationBean.class);
                    assertThat(context).hasSingleBean(AuditMethodInterceptor.class);
                });
    }

    @Test
    void shouldDisableHttpFilter() {
        contextRunner.withUserConfiguration(PipelineConfiguration.class)
                .withPropertyValues("audit.capture.http.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(AuditHttpFilter.class);
                    assertThat(context).doesNotHaveBean(FilterRegistrationBean.class);
                    assertThat(context).hasSingleBean(AuditMethodInterceptor.class);
                });
    }

    @Test
    void shouldDisableMethodInterceptor() {
        contextRunner.withUserConfiguration(PipelineConfiguration.class)
                .withPropertyValues("audit.capture.method.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(AuditHttpFilter.class);
                    assertThat(context).doesNotHaveBean(AuditMethodInterceptor.class);
                });
    }

    @Configuration
    static class PipelineConfiguration {
        @Bean
        io.safeaudit.core.spi.AuditStorage auditStorage() {
            return mock(io.safeaudit.core.spi.AuditStorage.class);
        }

        @Bean
        public AuditProcessingPipeline pipeline() {
            return mock(AuditProcessingPipeline.class);
        }

        @Bean
        @Primary
        public AuditEventCapture auditEventCapture() {
            return mock(AuditEventCapture.class);
        }
    }
}
