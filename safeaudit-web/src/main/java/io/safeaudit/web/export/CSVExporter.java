package io.safeaudit.web.export;

import io.safeaudit.core.domain.AuditEvent;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author Nelson Tanko
 */
public class CSVExporter {

    private static final String[] HEADERS = {
            "Event ID",
            "Timestamp",
            "Event Type",
            "Severity",
            "User ID",
            "Username",
            "IP Address",
            "Resource",
            "Action",
            "HTTP Status",
            "Application"
    };

    public byte[] export(List<AuditEvent> events) {
        var outputStream = new ByteArrayOutputStream();

        try (var writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
             var csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(HEADERS))) {

            for (var event : events) {
                csvPrinter.printRecord(
                        event.eventId(),
                        event.timestamp(),
                        event.eventType(),
                        event.severity(),
                        event.userId(),
                        event.username(),
                        event.ipAddress(),
                        event.resource(),
                        event.action(),
                        event.httpStatusCode(),
                        event.applicationName()
                );
            }

            csvPrinter.flush();
        } catch (IOException e) {
            throw new RuntimeException("CSV export failed", e);
        }

        return outputStream.toByteArray();
    }
}