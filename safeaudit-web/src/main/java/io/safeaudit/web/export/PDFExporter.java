package io.safeaudit.web.export;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import io.safeaudit.core.domain.AuditEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Nelson Tanko
 * @since 1.0.0
 */
public class PDFExporter {

    private static final Logger log = LoggerFactory.getLogger(PDFExporter.class);

    public byte[] export(List<AuditEvent> events, LocalDate from, LocalDate to) {
        var outputStream = new ByteArrayOutputStream();

        try (var writer = new PdfWriter(outputStream);
             var pdf = new PdfDocument(writer);
             var document = new Document(pdf)) {

            addHeader(document, from, to);

            addSummary(document, events);

            addEventTable(document, events);

            addFooter(document);

        } catch (IOException e) {
            log.error("Failed to generate PDF", e);
            throw new RuntimeException("PDF generation failed", e);
        }

        return outputStream.toByteArray();
    }

    private void addHeader(Document document, LocalDate from, LocalDate to) {
        var title = new Paragraph("Audit Log Report")
                .setFontSize(20)
                .setTextAlignment(TextAlignment.CENTER);
        document.add(title);

        var period = new Paragraph(
                String.format("Period: %s to %s",
                        from.format(DateTimeFormatter.ISO_DATE),
                        to.format(DateTimeFormatter.ISO_DATE)))
                .setFontSize(12)
                .setTextAlignment(TextAlignment.CENTER);
        document.add(period);

        document.add(new Paragraph("\n"));
    }

    private void addSummary(Document document, List<AuditEvent> events) {
        document.add(new Paragraph("Summary").setFontSize(14));

        Map<String, Long> eventsByType = events.stream()
                .collect(Collectors.groupingBy(AuditEvent::eventType, Collectors.counting()));

        Map<String, Long> eventsBySeverity = events.stream()
                .collect(Collectors.groupingBy(e -> e.severity().name(), Collectors.counting()));

        var summaryTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                .useAllAvailableWidth();

        summaryTable.addHeaderCell("Metric");
        summaryTable.addHeaderCell("Count");

        summaryTable.addCell("Total Events");
        summaryTable.addCell(String.valueOf(events.size()));

        eventsByType.forEach((eventType, _) ->
                summaryTable.addCell("Event Type" + eventType));

        eventsBySeverity.forEach((severity, count) -> {
            summaryTable.addCell("Severity: " + severity);
            summaryTable.addCell(String.valueOf(count));
        });

        document.add(summaryTable);
        document.add(new Paragraph("\n"));
    }

    private void addEventTable(Document document, List<AuditEvent> events) {
        document.add(new Paragraph("Detailed Events").setFontSize(14));

        var table = new Table(UnitValue.createPercentArray(new float[]{2, 2, 1, 3, 2}))
                .useAllAvailableWidth();

        table.addHeaderCell("Timestamp");
        table.addHeaderCell("User");
        table.addHeaderCell("Severity");
        table.addHeaderCell("Resource");
        table.addHeaderCell("Action");

        for (AuditEvent event : events) {
            table.addCell(event.timestamp().toString());
            table.addCell(event.username() != null ? event.username() : "N/A");
            table.addCell(event.severity().name());
            table.addCell(event.resource());
            table.addCell(event.action());
        }

        document.add(table);
    }

    private void addFooter(Document document) {
        document.add(new Paragraph("\n"));
        Paragraph footer = new Paragraph(
                String.format("Generated on %s by Audit Framework",
                        LocalDate.now().format(DateTimeFormatter.ISO_DATE)))
                .setFontSize(9)
                .setTextAlignment(TextAlignment.CENTER);
        document.add(footer);
    }
}
