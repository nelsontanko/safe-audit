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
    public static final String AUDITED_ANNOTATION_ATTRIBUTE = "io.safeaudit.web.capture.AUDITED_ANNOTATION";

    @Override
    public boolean preHandle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler) {

        if (handler instanceof HandlerMethod handlerMethod) {
            var method = handlerMethod.getMethod();
            Audited audited = method.getAnnotation(Audited.class);
            if (audited == null) {
                audited = handlerMethod.getBeanType().getAnnotation(Audited.class);
            }

            if (audited != null) {
                request.setAttribute(SHOULD_AUDIT_ATTRIBUTE, true);
                request.setAttribute(AUDITED_ANNOTATION_ATTRIBUTE, audited);
            }
        }

        return true;
    }
}
