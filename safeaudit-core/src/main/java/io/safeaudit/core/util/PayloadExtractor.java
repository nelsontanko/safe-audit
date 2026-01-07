package io.safeaudit.core.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author Nelson Tanko
 * @since 1.0.0
 */
public final class PayloadExtractor {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private PayloadExtractor() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Extract request body as string.
     */
    public static String extractRequestBody(HttpServletRequest request, int maxSize) {
        if (!(request instanceof ContentCachingRequestWrapper wrapper)) {
            return null;
        }

        byte[] content = wrapper.getContentAsByteArray();
        if (content.length == 0) {
            return null;
        }

        int length = Math.min(content.length, maxSize);
        String body = new String(content, 0, length, StandardCharsets.UTF_8);

        return sanitize(body);
    }

    /**
     * Extract response body as string.
     */
    public static String extractResponseBody(ContentCachingResponseWrapper response, int maxSize) {
        byte[] content = response.getContentAsByteArray();
        if (content.length == 0) {
            return null;
        }

        int length = Math.min(content.length, maxSize);
        String body = new String(content, 0, length, StandardCharsets.UTF_8);

        return sanitize(body);
    }

    /**
     * Sanitize payload - ensure valid JSON if possible.
     */
    private static String sanitize(String payload) {
        if (payload == null || payload.isBlank()) {
            return null;
        }

        try {
            Object parsed = OBJECT_MAPPER.readValue(payload, Object.class);
            return OBJECT_MAPPER.writeValueAsString(parsed);
        } catch (IOException _) {
            // Not JSON, return as-is (truncated)
            return payload;
        }
    }
}
