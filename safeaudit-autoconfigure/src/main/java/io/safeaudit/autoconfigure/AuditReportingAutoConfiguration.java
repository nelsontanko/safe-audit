package io.safeaudit.autoconfigure;

import io.safeaudit.core.config.AuditProperties;
import io.safeaudit.core.spi.AuditStorage;
import io.safeaudit.web.api.AuditExportController;
import io.safeaudit.web.api.AuditHealthController;
import io.safeaudit.web.api.AuditQueryController;
import io.safeaudit.web.export.CSVExporter;
import io.safeaudit.web.export.PDFExporter;
import io.safeaudit.web.ui.AuditDashboardController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author Nelson Tanko
 */
@AutoConfiguration
@ConditionalOnWebApplication
@ConditionalOnBean(AuditStorage.class)
public class AuditReportingAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(AuditReportingAutoConfiguration.class);

    /**
     * Query controller for REST API.
     */
    @Bean
    @ConditionalOnProperty(prefix = "audit.reporting.api", name = "enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean
    public AuditQueryController auditQueryController(
            AuditStorage storage,
            AuditProperties properties) {

        log.info("Registering audit query API at {}/events",
                properties.getReporting().getApi().getBasePath());
        return new AuditQueryController(storage, properties);
    }

    /**
     * Export controller for PDF/CSV.
     */
    @Bean
    @ConditionalOnProperty(prefix = "audit.reporting.api", name = "enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean
    public AuditExportController auditExportController(
            AuditStorage storage,
            PDFExporter pdfExporter,
            CSVExporter csvExporter) {

        log.info("Registering audit export API");
        return new AuditExportController(storage, pdfExporter, csvExporter);
    }

    /**
     * Health controller.
     */
    @Bean
    @ConditionalOnProperty(prefix = "audit.reporting.api", name = "enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean
    public AuditHealthController auditHealthController(AuditStorage storage) {
        return new AuditHealthController(storage);
    }

    /**
     * PDF exporter.
     */
    @Bean
    @ConditionalOnClass(name = "com.itextpdf.kernel.pdf.PdfDocument")
    @ConditionalOnMissingBean
    public PDFExporter pdfExporter() {
        log.info("Registering PDF exporter");
        return new PDFExporter();
    }

    /**
     * CSV exporter.
     */
    @Bean
    @ConditionalOnClass(name = "org.apache.commons.csv.CSVFormat")
    @ConditionalOnMissingBean
    public CSVExporter csvExporter() {
        log.info("Registering CSV exporter");
        return new CSVExporter();
    }

    /**
     * Dashboard controller.
     */
    @Bean
    @ConditionalOnProperty(prefix = "audit.reporting.ui", name = "enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean
    public AuditDashboardController auditDashboardController() {
        log.info("Registering audit dashboard UI");
        return new AuditDashboardController();
    }

    /**
     * Configure view resolver for dashboard.
     */
    @Bean
    @ConditionalOnProperty(prefix = "audit.reporting.ui", name = "enabled", havingValue = "true", matchIfMissing = true)
    public WebMvcConfigurer auditDashboardConfigurer(AuditProperties properties) {
        return new WebMvcConfigurer() {
            @Override
            public void addViewControllers(ViewControllerRegistry registry) {
                String path = properties.getReporting().getUi().getPath();
                log.info("Mapping audit dashboard to: {}", path);
                registry.addViewController(path).setViewName("audit-dashboard");
            }
        };
    }
}