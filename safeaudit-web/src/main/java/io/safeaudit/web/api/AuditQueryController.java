package io.safeaudit.web.api;

import io.safeaudit.core.config.AuditProperties;
import io.safeaudit.core.domain.QueryCriteria;
import io.safeaudit.core.domain.enums.AuditSeverity;
import io.safeaudit.core.spi.AuditStorage;
import io.safeaudit.web.dto.AuditEventDTO;
import io.safeaudit.web.dto.AuditStatsDTO;
import io.safeaudit.web.dto.PageDTO;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Set;

/**
 * @author Nelson Tanko
 * @since 1.0.0
 */
@RestController
@RequestMapping("${audit.reporting.api.base-path:/audit}")
public class AuditQueryController {

    private final AuditStorage storage;
    private final AuditProperties properties;

    public AuditQueryController(AuditStorage storage, AuditProperties properties) {
        this.storage = storage;
        this.properties = properties;
    }

    /**
     * Query audit events with filters and pagination.
     */
    @GetMapping("/events")
    public ResponseEntity<PageDTO<AuditEventDTO>> queryEvents(
            @RequestParam(required = false) String eventId,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String resource,
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) Set<AuditSeverity> severities,
            @RequestParam(required = false) String tenantId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "event_timestamp") String sortBy,
            @RequestParam(defaultValue = "DESC") QueryCriteria.SortDirection sortDirection) {

        // Validate and sanitize inputs
        size = Math.min(size, 1000); // Cap at 1000
        page = Math.max(page, 0);

        var criteria = QueryCriteria.builder()
                .eventId(eventId)
                .userId(userId)
                .username(username)
                .resource(resource)
                .eventType(eventType)
                .severities(severities)
                .tenantId(tenantId)
                .from(from)
                .to(to)
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .build();

        var events = storage.query(criteria);
        long total = storage.count(criteria);

        var dtos = events.stream()
                .map(AuditEventDTO::from)
                .toList();

        var pageDTO = new PageDTO<>(
                dtos,
                page,
                size,
                total,
                (int) Math.ceil((double) total / size)
        );

        return ResponseEntity.ok(pageDTO);
    }

    /**
     * Get a single audit event by ID.
     */
    @GetMapping("/events/{eventId}")
    public ResponseEntity<AuditEventDTO> getEvent(@PathVariable String eventId) {
        return storage.findById(eventId)
                .map(AuditEventDTO::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get audit statistics.
     */
    @GetMapping("/stats")
    public ResponseEntity<AuditStatsDTO> getStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {

        var criteria = QueryCriteria.builder()
                .from(from)
                .to(to)
                .build();

        long totalEvents = storage.count(criteria);

        long infoCount = storage.count(
                QueryCriteria.builder()
                        .from(from)
                        .to(to)
                        .severity(AuditSeverity.INFO)
                        .build()
        );

        long warnCount = storage.count(
                QueryCriteria.builder()
                        .from(from)
                        .to(to)
                        .severity(AuditSeverity.WARN)
                        .build()
        );

        long criticalCount = storage.count(
                QueryCriteria.builder()
                        .from(from)
                        .to(to)
                        .severity(AuditSeverity.CRITICAL)
                        .build()
        );

        var stats = new AuditStatsDTO(
                totalEvents,
                infoCount,
                warnCount,
                criticalCount,
                from,
                to
        );

        return ResponseEntity.ok(stats);
    }
}