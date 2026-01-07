package io.safeaudit.web.ui;

import io.safeaudit.core.config.AuditProperties;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Dashboard controller that serves the audit dashboard HTML directly.
 * This avoids requiring Thymeleaf or other template engines in consumer apps.
 *
 * @author Nelson Tanko
 * @since 1.0.0
 */
@RestController
@RequestMapping("${audit.reporting.ui.path:/audit/dashboard}")
public class AuditDashboardController {

    private static final String DASHBOARD_TEMPLATE = "templates/audit-dashboard.html";
    private static final String API_BASE_PLACEHOLDER = "const API_BASE = '/audit';";

    private final AuditProperties properties;

    public AuditDashboardController(AuditProperties properties) {
        this.properties = properties;
    }

    public AuditDashboardController() {
        this.properties = null;
    }

    @GetMapping(produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> dashboard() throws IOException {
        var resource = new ClassPathResource(DASHBOARD_TEMPLATE);
        var html = resource.getContentAsString(StandardCharsets.UTF_8);

        var apiBasePath = getApiBasePath();
        html = html.replace(API_BASE_PLACEHOLDER, "const API_BASE = '" + apiBasePath + "';");

        return ResponseEntity.ok(html);
    }

    private String getApiBasePath() {
        if (properties != null && properties.getReporting() != null
                && properties.getReporting().getApi() != null) {
            return properties.getReporting().getApi().getBasePath();
        }
        return "/audit";
    }
}