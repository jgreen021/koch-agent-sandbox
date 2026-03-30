package com.koch.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = com.koch.anomaly.AnomalyTrackerApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test") // Use test profile to avoid external dependencies if any
public class SecurityFilterChainTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testPublicEndpoints_Allowed() throws Exception {
        // Assume an endpoint doesn't exist but is permitted in config. We'll get 404/400 but NOT 401.
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk());
    }

    @Test
    public void testProtectedEndpoint_WithoutAuth_Returns401() throws Exception {
        mockMvc.perform(get("/api/sensors/readings"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "operator", authorities = {"ROLE_OPERATOR"})
    public void testProtectedEndpoint_WithValidAuth_Returns200() throws Exception {
        mockMvc.perform(get("/api/sensors/readings?assetId=KILN-01"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "random", authorities = {"ROLE_UNKNOWN"})
    public void testProtectedEndpoint_WithInvalidRole_Returns403() throws Exception {
        // PreAuthorize is checking for OPERATOR, AUDITOR or GATEWAY_ADMIN
        mockMvc.perform(get("/api/sensors/readings"))
                .andExpect(status().isForbidden());
    }
}
