package io.safeaudit.autoconfigure;

import io.safeaudit.core.spi.AuditStorage;
import io.safeaudit.web.api.AuditExportController;
import io.safeaudit.web.api.AuditHealthController;
import io.safeaudit.web.api.AuditQueryController;
import io.safeaudit.web.export.CSVExporter;
import io.safeaudit.web.export.PDFExporter;
import io.safeaudit.web.ui.AuditDashboardController;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class AuditReportingAutoConfigurationTest {

    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AuditReportingAutoConfiguration.class, AuditAutoConfiguration.class));

    @Test
    void shouldRegisterReportingBeansByDefault() {
        contextRunner.withUserConfiguration(StorageConfiguration.class)
                .run(context -> {
                    // API
                    assertThat(context).hasSingleBean(AuditQueryController.class);
                    assertThat(context).hasSingleBean(AuditExportController.class);
                    assertThat(context).hasSingleBean(AuditHealthController.class);
                    
                    // Exporters
                    assertThat(context).hasSingleBean(CSVExporter.class);
                    assertThat(context).hasSingleBean(PDFExporter.class);
                    
                    // UI
                    assertThat(context).hasSingleBean(AuditDashboardController.class);
                });
    }

    @Test
    void shouldDisableApi() {
        contextRunner.withUserConfiguration(StorageConfiguration.class)
                .withPropertyValues("audit.reporting.api.enabled=false")
                .run(context -> {
                    // API disabled
                    assertThat(context).doesNotHaveBean(AuditQueryController.class);
                    assertThat(context).doesNotHaveBean(AuditExportController.class);
                    assertThat(context).doesNotHaveBean(AuditHealthController.class);
                    
                    // UI still enabled
                    assertThat(context).hasSingleBean(AuditDashboardController.class);
                });
    }

    @Test
    void shouldDisableUi() {
        contextRunner.withUserConfiguration(StorageConfiguration.class)
                .withPropertyValues("audit.reporting.ui.enabled=false")
                .run(context -> {
                    // API still enabled
                    assertThat(context).hasSingleBean(AuditQueryController.class);
                    
                    // UI disabled
                    assertThat(context).doesNotHaveBean(AuditDashboardController.class);
                });
    }

    @Configuration
    static class StorageConfiguration {
        @Bean
        public AuditStorage auditStorage() {
            return mock(AuditStorage.class);
        }
    }
}
