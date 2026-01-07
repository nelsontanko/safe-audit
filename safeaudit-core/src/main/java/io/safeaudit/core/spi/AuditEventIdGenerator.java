package io.safeaudit.core.spi;

/**
 * @author Nelson Tanko
 * @since 1.0.0
 */
@FunctionalInterface
public interface AuditEventIdGenerator {

    /**
     * Generate a unique event ID.
     *
     * @return unique event identifier
     */
    String generate();
}
