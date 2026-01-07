package io.safeaudit.web.api;

import io.safeaudit.core.domain.HealthStatus;
import io.safeaudit.core.spi.AuditStorage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Nelson Tanko
 * @since 1.0.0
 */
@WebMvcTest(AuditHealthController.class)
class AuditHealthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuditStorage auditStorage;

    @Test
    void shouldReturnHealthyStatus() throws Exception {
        // Given
        when(auditStorage.checkHealth()).thenReturn(HealthStatus.healthy());

        // When/Then
        mockMvc.perform(get("/audit/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.component").value("audit"))
                .andExpect(jsonPath("$.message").value("OK"))
                .andDo(print());
    }

    @Test
    void shouldReturnUnhealthyStatus() throws Exception {
        // Given
        when(auditStorage.checkHealth()).thenReturn(HealthStatus.unhealthy("audit", "Connection failed"));

        // When/Then
        mockMvc.perform(get("/audit/health"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value("DOWN"))
                .andExpect(jsonPath("$.component").value("audit"))
                .andExpect(jsonPath("$.message").value("Connection failed"))
                .andDo(print());
    }
}
