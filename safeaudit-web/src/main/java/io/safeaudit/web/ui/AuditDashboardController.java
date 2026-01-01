package io.safeaudit.web.ui;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author Nelson Tanko
 */
@Controller
@RequestMapping("${audit.reporting.ui.path:/audit/dashboard}")
public class AuditDashboardController {

    @GetMapping
    public String dashboard() {
        return "audit-dashboard";
    }
}