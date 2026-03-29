package com.koch.anomaly;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AssetSensorKafkaListenerTest {

    @Mock
    private SseConnectionManager sseConnectionManager;

    @Mock
    private AssetSensorReadingRepository repository;

    @Mock
    private org.springframework.kafka.core.KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private AssetSensorKafkaListener listener;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testConsumeReading_BroadcatsToSse() throws Exception {
        String payload = "{\"assetId\":\"K-01\",\"status\":\"NORMAL\"}";
        AssetSensorReading reading = new AssetSensorReading(null, "K-01", "TEMP", 100.0, "C", LocalDateTime.now(), "NORMAL");
        
        when(objectMapper.readValue(payload, AssetSensorReading.class)).thenReturn(reading);
        
        listener.consumeReading(payload);

        verify(sseConnectionManager).broadcast(eq("K-01"), eq(payload));
    }

    @Test
    void testConsumeReading_PersistsAndForwardsIfCritical() throws Exception {
        String payload = "{\"assetId\":\"K-01\",\"status\":\"CRITICAL\"}";
        AssetSensorReading reading = new AssetSensorReading(1, "K-01", "TEMP", 300.0, "C", LocalDateTime.now(), "CRITICAL");
        
        when(objectMapper.readValue(payload, AssetSensorReading.class)).thenReturn(reading);
        
        listener.consumeReading(payload);

        // Verify broadcast still happens
        verify(sseConnectionManager).broadcast(eq("K-01"), eq(payload));
        // Verify database persistence
        verify(repository).save(org.mockito.ArgumentMatchers.any());
        // Verify alarm forwarding
        verify(kafkaTemplate).send(eq("active-alarms"), eq("K-01"), eq(payload));
    }
}
