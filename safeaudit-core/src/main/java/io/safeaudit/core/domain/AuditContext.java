package io.safeaudit.core.domain;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author Nelson Tanko
 */
public final class AuditContext {

    private static final ThreadLocal<Map<String, Object>> CONTEXT = ThreadLocal.withInitial(HashMap::new);

    private AuditContext() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void set(String key, Object value) {
        CONTEXT.get().put(key, value);
    }

    @SuppressWarnings("unchecked")
    public static <T> Optional<T> get(String key, Class<T> type) {
        Object value = CONTEXT.get().get(key);
        if (type.isInstance(value)) {
            return Optional.of((T) value);
        }
        return Optional.empty();
    }

    public static Optional<Object> get(String key) {
        return Optional.ofNullable(CONTEXT.get().get(key));
    }

    public static void clear() {
        CONTEXT.remove();
    }

    public static Map<String, Object> getAll() {
        return new HashMap<>(CONTEXT.get());
    }

    public static final String USER_ID = "userId";
    public static final String USERNAME = "username";
    public static final String SESSION_ID = "sessionId";
    public static final String TENANT_ID = "tenantId";
    public static final String IP_ADDRESS = "ipAddress";
    public static final String CORRELATION_ID = "correlationId";
    public static final String REQUEST_URI = "requestUri";
    public static final String HTTP_METHOD = "httpMethod";
}

