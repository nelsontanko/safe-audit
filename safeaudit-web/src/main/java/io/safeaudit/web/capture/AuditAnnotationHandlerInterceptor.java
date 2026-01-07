package io.safeaudit.web.capture;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor that checks for the presence of {@link Audited} annotation
 * and flags the request for auditing.
 *
 * @author Nelson Tanko
 * @since 1.0.0
 */
public class AuditAnnotationHandlerInterceptor implements HandlerInterceptor {

    public static final String SHOULD_AUDIT_ATTRIBUTE = "io.safeaudit.web.capture.SHOULD_AUDIT";

    @Override
    public boolean preHandle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler) {

        if (handler instanceof HandlerMethod handlerMethod) {
            var method = handlerMethod.getMethod();
            if (method.isAnnotationPresent(Audited.class) ||
                    handlerMethod.getBeanType().isAnnotationPresent(Audited.class)) {
                request.setAttribute(SHOULD_AUDIT_ATTRIBUTE, true);
            }
        }

        return true;
    }
}
