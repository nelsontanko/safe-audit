package io.safeaudit.core.spi;

import io.safeaudit.core.domain.enums.AuditSeverity;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

/**
 * Query criteria for filtering audit events.
 *
 * @author Nelson Tanko
 */
public final class QueryCriteria {

    private final String eventId;
    private final String userId;
    private final String username;
    private final String resource;
    private final String eventType;
    private final Set<AuditSeverity> severities;
    private final String tenantId;
    private final Instant from;
    private final Instant to;
    private final int page;
    private final int size;
    private final String sortBy;
    private final SortDirection sortDirection;

    private QueryCriteria(Builder builder) {
        this.eventId = builder.eventId;
        this.userId = builder.userId;
        this.username = builder.username;
        this.resource = builder.resource;
        this.eventType = builder.eventType;
        this.severities = Set.copyOf(builder.severities);
        this.tenantId = builder.tenantId;
        this.from = builder.from;
        this.to = builder.to;
        this.page = builder.page;
        this.size = builder.size;
        this.sortBy = builder.sortBy;
        this.sortDirection = builder.sortDirection;
    }

    public String getEventId() {
        return eventId;
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getResource() {
        return resource;
    }

    public String getEventType() {
        return eventType;
    }

    public Set<AuditSeverity> getSeverities() {
        return severities;
    }

    public String getTenantId() {
        return tenantId;
    }

    public Instant getFrom() {
        return from;
    }

    public Instant getTo() {
        return to;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public String getSortBy() {
        return sortBy;
    }

    public SortDirection getSortDirection() {
        return sortDirection;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String eventId;
        private String userId;
        private String username;
        private String resource;
        private String eventType;
        private final Set<AuditSeverity> severities = new HashSet<>();
        private String tenantId;
        private Instant from;
        private Instant to;
        private int page = 0;
        private int size = 50;
        private String sortBy = "event_timestamp";
        private SortDirection sortDirection = SortDirection.DESC;

        public Builder eventId(String eventId) {
            this.eventId = eventId;
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

        public Builder resource(String resource) {
            this.resource = resource;
            return this;
        }

        public Builder eventType(String eventType) {
            this.eventType = eventType;
            return this;
        }

        public Builder severity(AuditSeverity severity) {
            this.severities.add(severity);
            return this;
        }

        public Builder severities(Set<AuditSeverity> severities) {
            this.severities.clear();
            if (severities != null) {
                this.severities.addAll(severities);
            }
            return this;
        }

        public Builder tenantId(String tenantId) {
            this.tenantId = tenantId;
            return this;
        }

        public Builder from(Instant from) {
            this.from = from;
            return this;
        }

        public Builder to(Instant to) {
            this.to = to;
            return this;
        }

        public Builder page(int page) {
            this.page = Math.max(0, page);
            return this;
        }

        public Builder size(int size) {
            this.size = Math.clamp(size, 1, 1000); // Cap at 1000
            return this;
        }

        public Builder sortBy(String sortBy) {
            this.sortBy = sortBy;
            return this;
        }

        public Builder sortDirection(SortDirection sortDirection) {
            this.sortDirection = sortDirection;
            return this;
        }

        public QueryCriteria build() {
            return new QueryCriteria(this);
        }
    }

    public enum SortDirection {
        ASC, DESC
    }
}
