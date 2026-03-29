package com.koch.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = com.koch.anomaly.AnomalyTrackerApplication.class)
@AutoConfigureMockMvc
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @org.springframework.test.context.bean.override.mockito.MockitoBean
    private com.koch.anomaly.AssetSensorReadingRepository repository;

    @org.springframework.test.context.bean.override.mockito.MockitoBean
    private com.koch.anomaly.KilnSensorProducer producer;

    @org.springframework.test.context.bean.override.mockito.MockitoBean
    private org.springframework.kafka.core.KafkaTemplate<String, String> kafkaTemplate;

    @org.springframework.test.context.bean.override.mockito.MockitoBean
    private JdbcUserRepository userRepository;

    @Test
    @DisplayName("Login with invalid credentials should return 401")
    void testLoginFlow_InvalidCredentials_Returns401() throws Exception {
        String payload = "{\"username\": \"admin\", \"password\": \"wrongpw\"}";
        
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Refresh token with missing token should return 400 or 401")
    void testRefreshToken_MissingToken() throws Exception {
        String payload = "{}"; // No refresh token provided
        
        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isUnauthorized());
    }

}
