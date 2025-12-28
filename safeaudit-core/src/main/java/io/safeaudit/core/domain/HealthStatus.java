package io.safeaudit.core.domain;

import java.util.Objects;

/**
 * @author Nelson Tanko
 */
public final class HealthStatus {

    private final boolean healthy;
    private final String message;
    private final String component;

    private HealthStatus(boolean healthy, String message, String component) {
        this.healthy = healthy;
        this.message = message;
        this.component = component;
    }

    public boolean isHealthy() {
        return healthy;
    }

    public String getMessage() {
        return message;
    }

    public String getComponent() {
        return component;
    }

    public static HealthStatus healthy() {
        return new HealthStatus(true, "OK", null);
    }

    public static HealthStatus healthy(String component) {
        return new HealthStatus(true, "OK", component);
    }

    public static HealthStatus unhealthy(String message) {
        return new HealthStatus(false, message, null);
    }

    public static HealthStatus unhealthy(String component, String message) {
        return new HealthStatus(false, message, component);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HealthStatus that = (HealthStatus) o;
        return healthy == that.healthy &&
                Objects.equals(message, that.message) &&
                Objects.equals(component, that.component);
    }

    @Override
    public int hashCode() {
        return Objects.hash(healthy, message, component);
    }

    @Override
    public String toString() {
        return "HealthStatus{" +
                "healthy=" + healthy +
                ", message='" + message + '\'' +
                ", component='" + component + '\'' +
                '}';
    }
}