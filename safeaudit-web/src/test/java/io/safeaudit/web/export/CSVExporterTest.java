package io.safeaudit.web.export;

import io.safeaudit.core.domain.AuditEvent;
import io.safeaudit.core.domain.enums.AuditSeverity;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CSVExporterTest {

    private final CSVExporter exporter = new CSVExporter();

    @Test
    void shouldExportEventsToCSV() {
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

        // When
        byte[] csvBytes = exporter.export(List.of(event));
        var csvContent = new String(csvBytes);

        // Then
        assertThat(csvContent)
                .contains("Event ID,Timestamp,Event Type,Severity,User ID,Username,IP Address,Resource,Action,HTTP Status,Application")
                .contains("event-1")
                .contains(timestamp.toString())
                .contains("LOGIN")
                .contains("INFO")
                .contains("user-1")
                .contains("john.doe")
                .contains("127.0.0.1")
                .contains("/api/login")
                .contains("POST")
                .contains("200")
                .contains("test-app");
    }

    @Test
    void shouldHandleEmptyList() {
        // When
        byte[] csvBytes = exporter.export(List.of());
        var csvContent = new String(csvBytes);

        // Then
        assertThat(csvContent)
                .contains("Event ID,Timestamp,Event Type,Severity,User ID,Username,IP Address,Resource,Action,HTTP Status,Application");
        // Verify only header remains (split by newline should be 1 or 2 depending on implementation)
    }
}
