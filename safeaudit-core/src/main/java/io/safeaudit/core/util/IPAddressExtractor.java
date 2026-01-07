package io.safeaudit.core.util;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Utility for extracting client IP address from HTTP requests.
 * Handles proxy headers (X-Forwarded-For, X-Real-IP).
 *
 * @author Nelson Tanko
 * @since 1.0.0
 */
public final class IPAddressExtractor {

    private static final String[] HEADERS = {
            "X-Forwarded-For",
            "X-Real-IP",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
    };

    private IPAddressExtractor() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Extract client IP address from HTTP request.
     * Checks proxy headers before falling back to remote address.
     */
    public static String extract(HttpServletRequest request) {
        if (request == null) {
            return null;
        }

        for (var header : HEADERS) {
            var ip = request.getHeader(header);
            if (isValidIP(ip)) {
                // Handle multiple IPs in X-Forwarded-For
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }
        }

        return request.getRemoteAddr();
    }

    private static boolean isValidIP(String ip) {
        return ip != null &&
                !ip.isEmpty() &&
                !"unknown".equalsIgnoreCase(ip);
    }
}
