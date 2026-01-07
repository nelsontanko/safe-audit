package io.safeaudit.core.processing.integrity;

import io.safeaudit.core.domain.AuditEvent;
import io.safeaudit.core.spi.AuditEventProcessor;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Calculates integrity hash for audit events.
 * Creates a chain by including the previous event's hash.
 *
 * @author Nelson Tanko
 * @since 1.0.0
 */
public class HashCalculator implements AuditEventProcessor {

    private final MessageDigest digest;
    private final boolean includePreviousHash;
    private volatile String lastHash = null;

    public HashCalculator(String algorithm, boolean includePreviousHash) {
        try {
            this.digest = MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("Hash algorithm not supported: " + algorithm, e);
        }
        this.includePreviousHash = includePreviousHash;
    }

    @Override
    public AuditEvent process(AuditEvent event) {
        var previousHash = includePreviousHash ? lastHash : null;
        var eventHash = calculateHash(event, previousHash);

        lastHash = eventHash;

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
                .requestPayload(event.requestPayload())
                .responsePayload(event.responsePayload())
                .httpStatusCode(event.httpStatusCode())
                .compliance(event.compliance())
                .previousEventHash(previousHash)
                .eventHash(eventHash)
                .capturedBy(event.capturedBy())
                .applicationName(event.applicationName())
                .applicationInstance(event.applicationInstance())
                .build();
    }

    private String calculateHash(AuditEvent event, String previousHash) {
        var content = new StringBuilder();

        content.append(event.eventId());
        content.append(event.eventHash());
        content.append(event.timestamp());
        content.append(event.eventType());
        content.append(event.userId());
        content.append(event.resource());
        content.append(event.action());
        content.append(event.sessionId());

        if (previousHash != null) {
            content.append(previousHash);
        }

        digest.reset();
        byte[] hash = digest.digest(content.toString().getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash);
    }

    @Override
    public int getOrder() {
        return 900; // Run last, after all modifications
    }
}