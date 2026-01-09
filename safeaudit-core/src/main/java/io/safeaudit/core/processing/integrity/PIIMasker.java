package io.safeaudit.core.processing.integrity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.safeaudit.core.config.AuditProperties;
import io.safeaudit.core.domain.AuditEvent;
import io.safeaudit.core.spi.AuditEventProcessor;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Objects;
import java.util.Set;

/**
 * @author Nelson Tanko
 * @since 1.0.0
 */
public class PIIMasker implements AuditEventProcessor {

    private final Set<String> piiFields;
    private final AuditProperties.PIIMaskingStrategy strategy;
    private final ObjectMapper objectMapper;
    private final MessageDigest digest;

    public PIIMasker(Set<String> piiFields, AuditProperties.PIIMaskingStrategy strategy) {
        this.piiFields = Set.copyOf(piiFields);
        this.strategy = strategy;
        this.objectMapper = new ObjectMapper();

        try {
            this.digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    @Override
    public AuditEvent process(AuditEvent event) {
        var maskedRequest = maskPayload(event.requestPayload());
        var maskedResponse = maskPayload(event.responsePayload());

        if (Objects.equals(maskedRequest, event.requestPayload()) &&
                Objects.equals(maskedResponse, event.responsePayload())) {
            return event;
        }

        return AuditEvent.builder()
                .eventId(event.eventId())
                .sequenceNumber(event.sequenceNumber())
                .timestamp(event.timestamp())
                .eventType(event.eventType())
                .severity(event.severity())
                .userId(event.userId())
                .username(event.username())
                .ipAddress(event.ipAddress())
                .userAgent(event.userAgent())
                .resource(event.resource())
                .action(event.action())
                .sessionId(event.sessionId())
                .tenantId(event.tenantId())
                .requestPayload(maskedRequest)
                .responsePayload(maskedResponse)
                .httpStatusCode(event.httpStatusCode())
                .compliance(event.compliance())
                .previousEventHash(event.previousEventHash())
                .eventHash(event.eventHash())
                .capturedBy(event.capturedBy())
                .applicationName(event.applicationName())
                .applicationInstance(event.applicationInstance())
                .build();
    }

    private String maskPayload(String payload) {
        if (payload == null || payload.isBlank()) {
            return payload;
        }

        try {
            JsonNode root = objectMapper.readTree(payload);
            if (root.isObject()) {
                maskFields((ObjectNode) root);
            }
            return objectMapper.writeValueAsString(root);
        } catch (Exception _) {
            return payload;
        }
    }

    private void maskFields(ObjectNode node) {
        node.fieldNames().forEachRemaining(fieldName -> {
            if (piiFields.contains(fieldName)) {
                JsonNode value = node.get(fieldName);
                if (value.isTextual()) {
                    var masked = maskValue(value.asText());
                    node.put(fieldName, masked);
                }
            } else {
                JsonNode child = node.get(fieldName);
                if (child.isObject()) {
                    maskFields((ObjectNode) child);
                }
            }
        });
    }

    private String maskValue(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }

        return switch (strategy) {
            case HASH -> hashValue(value);
            case MASK -> maskPartial(value);
            case REDACT -> "[REDACTED]";
        };
    }

    private String hashValue(String value) {
        byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
        return "SHA256:" + Base64.getEncoder().encodeToString(hash);
    }

    private String maskPartial(String value) {
        if (value.length() <= 4) {
            return "****";
        }
        var prefix = value.substring(0, 2);
        var suffix = value.substring(value.length() - 2);
        return prefix + "****" + suffix;
    }

    @Override
    public int getOrder() {
        return 300; // Run after enrichment
    }
}
