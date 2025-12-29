package io.safeaudit.web.api;

import io.safeaudit.core.spi.AuditStorage;
import io.safeaudit.core.spi.QueryCriteria;
import io.safeaudit.web.export.CSVExporter;
import io.safeaudit.web.export.PDFExporter;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.ZoneOffset;

/**
 * @author Nelson Tanko
 */
@RestController
@RequestMapping("${audit.reporting.api.base-path:/audit}")
public class AuditExportController {

    private final AuditStorage storage;
    private final PDFExporter pdfExporter;
    private final CSVExporter csvExporter;

    public AuditExportController(
            AuditStorage storage,
            PDFExporter pdfExporter,
            CSVExporter csvExporter) {
        this.storage = storage;
        this.pdfExporter = pdfExporter;
        this.csvExporter = csvExporter;
    }

    /**
     * Export audit events to PDF.
     */
    @GetMapping("/export/pdf")
    public ResponseEntity<Resource> exportPDF(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String resource) {

        var criteria = buildCriteria(from, to, userId, resource);
        var events = storage.query(criteria);

        var pdfBytes = pdfExporter.export(events, from, to);
        var arrayResource = new ByteArrayResource(pdfBytes);

        var filename = String.format("audit-log-%s-to-%s.pdf", from, to);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdfBytes.length)
                .body(arrayResource);
    }

    /**
     * Export audit events to CSV.
     */
    @GetMapping("/export/csv")
    public ResponseEntity<Resource> exportCSV(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String resource) {

        var criteria = buildCriteria(from, to, userId, resource);
        var events = storage.query(criteria);

        var csvBytes = csvExporter.export(events);
        var resourceEntity = new ByteArrayResource(csvBytes);

        var filename = String.format("audit-log-%s-to-%s.csv", from, to);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .contentLength(csvBytes.length)
                .body(resourceEntity);
    }

    private QueryCriteria buildCriteria(LocalDate from, LocalDate to, String userId, String resource) {
        var fromInstant = from.atStartOfDay().toInstant(ZoneOffset.UTC);
        var toInstant = to.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);

        return QueryCriteria.builder()
                .from(fromInstant)
                .to(toInstant)
                .userId(userId)
                .resource(resource)
                .size(10000) // Max export size
                .build();
    }
}