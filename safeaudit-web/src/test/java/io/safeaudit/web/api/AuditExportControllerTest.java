package io.safeaudit.web.api;

import io.safeaudit.core.domain.AuditEvent;
import io.safeaudit.core.domain.enums.AuditSeverity;
import io.safeaudit.core.spi.AuditStorage;
import io.safeaudit.core.spi.QueryCriteria;
import io.safeaudit.web.export.CSVExporter;
import io.safeaudit.web.export.PDFExporter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @author Nelson Tanko
 * @since 1.0.0
 */
@WebMvcTest(AuditExportController.class)
class AuditExportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuditStorage auditStorage;

    @MockitoBean
    private PDFExporter pdfExporter;

    @MockitoBean
    private CSVExporter csvExporter;

    @Test
    void shouldExportPDF() throws Exception {
        // Given
        var now = LocalDate.now();
        var event = AuditEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .timestamp(Instant.now())
                .eventType("TEST")
                .severity(AuditSeverity.INFO)
                .build();

        byte[] pdfContent = "PDF CONTENT".getBytes();

        when(auditStorage.query(any(QueryCriteria.class))).thenReturn(List.of(event));
        when(pdfExporter.export(any(), any(), any())).thenReturn(pdfContent);

        // When/Then
        mockMvc.perform(get("/audit/export/pdf")
                        .param("from", now.toString())
                        .param("to", now.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"audit-log-" + now + "-to-" + now + ".pdf\""))
                .andExpect(content().bytes(pdfContent));
    }

    @Test
    void shouldExportCSV() throws Exception {
        // Given
        var now = LocalDate.now();
        var event = AuditEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .timestamp(Instant.now())
                .eventType("TEST")
                .severity(AuditSeverity.INFO)
                .build();

        byte[] csvContent = "CSV,CONTENT".getBytes();

        when(auditStorage.query(any(QueryCriteria.class))).thenReturn(List.of(event));
        when(csvExporter.export(any())).thenReturn(csvContent);

        // When/Then
        mockMvc.perform(get("/audit/export/csv")
                        .param("from", now.toString())
                        .param("to", now.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/csv"))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"audit-log-" + now + "-to-" + now + ".csv\""))
                .andExpect(content().bytes(csvContent));
    }
}
