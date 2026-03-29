package com.koch.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = com.koch.anomaly.AnomalyTrackerApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Verify Swagger UI is accessible publicly")
    void testSwaggerAccessibility() throws Exception {
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().exists("Location"));
    }

    @Test
    @DisplayName("Verify API docs are accessible publicly")
    void testApiDocsAccessibility() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Verify unauthenticated request to sensors returns 401")
    void testUnauthenticatedSensors() throws Exception {
        mockMvc.perform(get("/api/sensors/readings"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Verify security headers are present")
    void testSecurityHeaders() throws Exception {
        mockMvc.perform(get("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"test\",\"password\":\"test\"}"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("X-XSS-Protection", "1; mode=block"))
                .andExpect(header().exists("Content-Security-Policy"));
    }
}
