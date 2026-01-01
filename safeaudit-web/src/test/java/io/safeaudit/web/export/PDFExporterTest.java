package io.safeaudit.web.export;

import io.safeaudit.core.domain.AuditEvent;
import io.safeaudit.core.domain.enums.AuditSeverity;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PDFExporterTest {

    private final PDFExporter exporter = new PDFExporter();

    @Test
    void shouldExportEventsToPDF() {
        // Given
        var timestamp = Instant.parse("2023-10-01T10:00:00Z");
        var event = AuditEvent.builder()
                .eventId("event-1")
                .timestamp(timestamp)
                .eventType("LOGIN")
                .severity(AuditSeverity.INFO)
                .userId("user-1")
                .username("john.doe")
                .ipAddress("127.0.0.1")
                .resource("/api/login")
                .action("POST")
                .httpStatusCode(200)
                .applicationName("test-app")
                .build();
        
        var from = LocalDate.now().minusDays(1);
        var to = LocalDate.now();

        // When
        byte[] pdfBytes = exporter.export(List.of(event), from, to);

        // Then
        assertThat(pdfBytes).isNotEmpty();
        // Check for PDF signature "%PDF-"
        assertThat(new String(pdfBytes)).startsWith("%PDF-");
    }

    @Test
    void shouldHandleEmptyList() {
        // Given
        var from = LocalDate.now().minusDays(1);
        var to = LocalDate.now();

        // When
        byte[] pdfBytes = exporter.export(List.of(), from, to);

        // Then
        assertThat(pdfBytes).isNotEmpty();
        assertThat(new String(pdfBytes)).startsWith("%PDF-");
    }
}
