package io.safeaudit.web.api;

import io.safeaudit.core.config.AuditProperties;
import io.safeaudit.core.domain.AuditEvent;
import io.safeaudit.core.domain.QueryCriteria;
import io.safeaudit.core.domain.enums.AuditSeverity;
import io.safeaudit.core.spi.AuditStorage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Nelson Tanko
 * @since 1.0.0
 */
@WebMvcTest(AuditQueryController.class)
class AuditQueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuditStorage auditStorage;

    @MockitoBean
    private AuditProperties auditProperties;

    @Test
    void shouldQueryEvents() throws Exception {
        // Given
        AuditEvent event = AuditEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .timestamp(Instant.now())
                .eventType("TEST")
                .severity(AuditSeverity.INFO)
                .userId("user1")
                .build();

        when(auditStorage.query(any(QueryCriteria.class))).thenReturn(List.of(event));
        when(auditStorage.count(any(QueryCriteria.class))).thenReturn(1L);

        // When/Then
        mockMvc.perform(get("/audit/events")
                        .param("userId", "user1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].eventId").value(event.eventId()))
                .andExpect(jsonPath("$.content[0].userId").value("user1"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    void shouldGetEventById() throws Exception {
        // Given
        String eventId = UUID.randomUUID().toString();
        AuditEvent event = AuditEvent.builder()
                .eventId(eventId)
                .timestamp(Instant.now())
                .eventType("TEST")
                .severity(AuditSeverity.INFO)
                .build();

        when(auditStorage.findById(eventId)).thenReturn(Optional.of(event));

        // When/Then
        mockMvc.perform(get("/audit/events/{eventId}", eventId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventId").value(eventId));
    }

    @Test
    void shouldReturn404ForUnknownEvent() throws Exception {
        // Given
        String eventId = "unknown";
        when(auditStorage.findById(eventId)).thenReturn(Optional.empty());

        // When/Then
        mockMvc.perform(get("/audit/events/{eventId}", eventId))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetStatistics() throws Exception {
        // Given
        when(auditStorage.count(any(QueryCriteria.class))).thenReturn(100L);

        // When/Then
        mockMvc.perform(get("/audit/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalEvents").value(100)); // Just verifying structure
    }
}
