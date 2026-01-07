package io.safeaudit.core.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.safeaudit.core.domain.enums.AuditSeverity;

import java.time.Instant;
import java.util.Objects;

/**
 * @author Nelson Tanko
 * @since 1.0.0
 */
public record AuditEvent(
        String eventId, long sequenceNumber, Instant timestamp, String eventType,

        AuditSeverity severity, String userId, String username, String ipAddress, String userAgent,

        String resource, String action, String sessionId, String tenantId, String requestPayload,

        String responsePayload, Integer httpStatusCode, ComplianceMetadata compliance,

        String previousEventHash, String eventHash, String capturedBy, String applicationName,

        String applicationInstance
) {

    @JsonCreator
    public AuditEvent(
            @JsonProperty("eventId") String eventId,
            @JsonProperty("sequenceNumber") long sequenceNumber,
            @JsonProperty("timestamp") Instant timestamp,
            @JsonProperty("eventType") String eventType,
            @JsonProperty("severity") AuditSeverity severity,
            @JsonProperty("userId") String userId,
            @JsonProperty("username") String username,
            @JsonProperty("ipAddress") String ipAddress,
            @JsonProperty("userAgent") String userAgent,
            @JsonProperty("resource") String resource,
            @JsonProperty("action") String action,
            @JsonProperty("sessionId") String sessionId,
            @JsonProperty("tenantId") String tenantId,
            @JsonProperty("requestPayload") String requestPayload,
            @JsonProperty("responsePayload") String responsePayload,
            @JsonProperty("httpStatusCode") Integer httpStatusCode,
            @JsonProperty("compliance") ComplianceMetadata compliance,
            @JsonProperty("previousEventHash") String previousEventHash,
            @JsonProperty("eventHash") String eventHash,
            @JsonProperty("capturedBy") String capturedBy,
            @JsonProperty("applicationName") String applicationName,
            @JsonProperty("applicationInstance") String applicationInstance) {

        this.eventId = Objects.requireNonNull(eventId, "eventId is required");
        this.sequenceNumber = sequenceNumber;
        this.timestamp = Objects.requireNonNull(timestamp, "timestamp is required");
        this.eventType = Objects.requireNonNull(eventType, "eventType is required");
        this.severity = Objects.requireNonNull(severity, "severity is required");
        this.userId = userId;
        this.username = username;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.resource = resource;
        this.action = action;
        this.sessionId = sessionId;
        this.tenantId = tenantId;
        this.requestPayload = requestPayload;
        this.responsePayload = responsePayload;
        this.httpStatusCode = httpStatusCode;
        this.compliance = compliance != null ? compliance : ComplianceMetadata.empty();
        this.previousEventHash = previousEventHash;
        this.eventHash = eventHash;
        this.capturedBy = capturedBy;
        this.applicationName = applicationName;
        this.applicationInstance = applicationInstance;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String eventId;
        private long sequenceNumber;
        private Instant timestamp;
        private String eventType;
        private AuditSeverity severity = AuditSeverity.INFO;
        private String userId;
        private String username;
        private String ipAddress;
        private String userAgent;
        private String resource;
        private String action;
        private String sessionId;
        private String tenantId;
        private String requestPayload;
        private String responsePayload;
        private Integer httpStatusCode;
        private ComplianceMetadata compliance;
        private String previousEventHash;
        private String eventHash;
        private String capturedBy;
        private String applicationName;
        private String applicationInstance;

        public Builder eventId(String eventId) {
            this.eventId = eventId;
            return this;
        }

        public Builder sequenceNumber(long sequenceNumber) {
            this.sequenceNumber = sequenceNumber;
            return this;
        }

        public Builder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder eventType(String eventType) {
            this.eventType = eventType;
            return this;
        }

        public Builder severity(AuditSeverity severity) {
            this.severity = severity;
            return this;
        }

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder ipAddress(String ipAddress) {
            this.ipAddress = ipAddress;
            return this;
        }

        public Builder userAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        public Builder resource(String resource) {
            this.resource = resource;
            return this;
        }

        public Builder action(String action) {
            this.action = action;
            return this;
        }

        public Builder sessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        public Builder tenantId(String tenantId) {
            this.tenantId = tenantId;
            return this;
        }

        public Builder requestPayload(String requestPayload) {
            this.requestPayload = requestPayload;
            return this;
        }

        public Builder responsePayload(String responsePayload) {
            this.responsePayload = responsePayload;
            return this;
        }

        public Builder httpStatusCode(Integer httpStatusCode) {
            this.httpStatusCode = httpStatusCode;
            return this;
        }

        public Builder compliance(ComplianceMetadata compliance) {
            this.compliance = compliance;
            return this;
        }

        public Builder previousEventHash(String previousEventHash) {
            this.previousEventHash = previousEventHash;
            return this;
        }

        public Builder eventHash(String eventHash) {
            this.eventHash = eventHash;
            return this;
        }

        public Builder capturedBy(String capturedBy) {
            this.capturedBy = capturedBy;
            return this;
        }

        public Builder applicationName(String applicationName) {
            this.applicationName = applicationName;
            return this;
        }

        public Builder applicationInstance(String applicationInstance) {
            this.applicationInstance = applicationInstance;
            return this;
        }

        public AuditEvent build() {
            return new AuditEvent(
                    eventId, sequenceNumber, timestamp, eventType, severity,
                    userId, username, ipAddress, userAgent,
                    resource, action, sessionId, tenantId,
                    requestPayload, responsePayload, httpStatusCode,
                    compliance, previousEventHash, eventHash,
                    capturedBy, applicationName, applicationInstance
            );
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuditEvent that = (AuditEvent) o;
        return Objects.equals(eventId, that.eventId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId);
    }

    @Override
    public String toString() {
        return "AuditEvent{" +
                "eventId='" + eventId + '\'' +
                ", timestamp=" + timestamp +
                ", eventType='" + eventType + '\'' +
                ", severity=" + severity +
                ", userId='" + userId + '\'' +
                ", resource='" + resource + '\'' +
                ", action='" + action + '\'' +
                '}';
    }
}
