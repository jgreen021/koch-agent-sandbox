package com.koch.anomaly;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import com.koch.security.AuditService;
import com.koch.security.RsaKeyService;
import com.koch.security.filter.WebAuditFilter;
import com.koch.security.SecurityConfig;
import com.koch.security.GlobalExceptionHandler;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.HttpMethod;
import com.koch.security.JdbcUserRepository;
import org.springframework.test.context.ActiveProfiles;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@SpringBootTest(classes = com.koch.anomaly.AnomalyTrackerApplication.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
public class KilnControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private KilnService kilnService;

    @MockitoBean
    private AuditService auditService;

    @MockitoBean
    private WebAuditFilter webAuditFilter;

    // Additional architectural mocks needed for full context load
    @MockitoBean
    private com.koch.anomaly.AssetSensorReadingRepository repository;

    @MockitoBean
    private com.koch.anomaly.KilnSensorProducer producer;

    @MockitoBean
    private org.springframework.kafka.core.KafkaTemplate<String, String> kafkaTemplate;

    @MockitoBean
    private JdbcUserRepository userRepository;

    @Test
    @DisplayName("Should return 201 Created when Admin posts valid KilnRequest")
    @WithMockUser(roles = "GATEWAY_ADMIN")
    void testCreateKilnHappyPath() throws Exception {
        String json = """
                {
                    "name": "Test Kiln",
                    "type": "ELECTRIC",
                    "isActive": true,
                    "baselineTemp": 200.0,
                    "warningTemp": 300.0,
                    "criticalTemp": 400.0,
                    "stateDurationSeconds": 30,
                    "warningProbability": 0.15,
                    "criticalProbability": 0.05
                }
                """;

        mockMvc.perform(post("/api/kilns")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
                .andExpect(status().isCreated());
    }
}
