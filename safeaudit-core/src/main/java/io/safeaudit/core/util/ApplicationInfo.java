package io.safeaudit.core.util;

import org.springframework.context.ApplicationContext;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author Nelson Tanko
 * @since 1.0.0
 */
public final class ApplicationInfo {

    private ApplicationInfo() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Get application name from Spring context.
     */
    public static String getApplicationName(ApplicationContext context) {
        if (context == null) {
            return "unknown";
        }

        var name = context.getEnvironment().getProperty("spring.application.name");
        return name != null ? name : context.getId();
    }

    /**
     * Get application instance identifier (hostname).
     */
    public static String getApplicationInstance() {
        try {
            var address = InetAddress.getLocalHost();
            return address.getHostName();
        } catch (UnknownHostException _) {
            return "unknown-host";
        }
    }

    /**
     * Get framework version.
     */
    public static String getFrameworkVersion() {
        var pkg = ApplicationInfo.class.getPackage();
        var version = pkg != null ? pkg.getImplementationVersion() : null;
        return version != null ? version : "dev";
    }
}