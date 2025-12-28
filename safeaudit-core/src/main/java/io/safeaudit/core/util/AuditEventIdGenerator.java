package io.safeaudit.core.util;

/**
 * @author Nelson Tanko
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
