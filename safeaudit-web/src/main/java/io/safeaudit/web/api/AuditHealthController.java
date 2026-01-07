package io.safeaudit.web.api;

import io.safeaudit.core.spi.AuditStorage;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author Nelson Tanko
 * @since 1.0.0
 */
@RestController
@RequestMapping("${audit.reporting.api.base-path:/audit}")
public class AuditHealthController {

    private final AuditStorage storage;

    public AuditHealthController(AuditStorage storage) {
        this.storage = storage;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        var status = storage.checkHealth();

        Map<String, Object> response = Map.of(
                "status", status.isHealthy() ? "UP" : "DOWN",
                "component", status.getComponent() != null ? status.getComponent() : "audit",
                "message", status.getMessage() != null ? status.getMessage() : "OK"
        );

        return status.isHealthy() ?
                ResponseEntity.ok(response) :
                ResponseEntity.status(503).body(response);
    }
}