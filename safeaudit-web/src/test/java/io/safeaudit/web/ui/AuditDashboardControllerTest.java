package io.safeaudit.web.ui;

import io.safeaudit.core.config.AuditProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Nelson Tanko
 * @since 1.0.0
 */
@WebMvcTest(AuditDashboardController.class)
class AuditDashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuditProperties auditProperties;

    @Test
    void shouldReturnDashboardView() throws Exception {
        mockMvc.perform(get("/audit/dashboard"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(org.springframework.http.MediaType.TEXT_HTML))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Audit Dashboard")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("statsContainer")));
    }
}
