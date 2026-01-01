package io.safeaudit.web.ui;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuditDashboardController.class)
class AuditDashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnDashboardView() throws Exception {
        mockMvc.perform(get("/audit/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("audit-dashboard"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Audit Dashboard")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("statsContainer")));
    }
}
