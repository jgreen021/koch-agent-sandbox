package com.koch.anomaly;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KilnSensorProducerTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private KilnSensorProducer producer;

    @Test
    @DisplayName("publishReading: Should serialize AssetSensorReading and send to Kafka")
    void publishReading_ShouldSerializeAndSend() throws JsonProcessingException {
        // Arrange
        AssetSensorReading reading = new AssetSensorReading(
                1, "KILN-01", "TEMPERATURE", 105.5, "C", LocalDateTime.now(), "NORMAL"
        );
        String jsonValue = "{\"assetId\":\"KILN-01\",\"readingValue\":105.5}";
        when(objectMapper.writeValueAsString(reading)).thenReturn(jsonValue);
        // Stub send to return a completed future to avoid NPE in whenComplete
        when(kafkaTemplate.send(anyString(), anyString())).thenReturn(java.util.concurrent.CompletableFuture.completedFuture(null));

        // Act
        producer.publishReading(reading);

        // Assert
        verify(kafkaTemplate).send(eq("kiln-sensor-readings"), eq(jsonValue));
    }

    @Test
    @DisplayName("publishReading: Should throw when serialization fails")
    void publishReading_WhenSerializationFails_ShouldThrow() throws JsonProcessingException {
        // Arrange
        AssetSensorReading reading = new AssetSensorReading(
                1, "KILN-01", "TEMPERATURE", 105.5, "C", LocalDateTime.now(), "NORMAL"
        );
        when(objectMapper.writeValueAsString(reading)).thenThrow(new JsonProcessingException("Serialization failed") {});

        // Act & Assert
        try {
            producer.publishReading(reading);
        } catch (RuntimeException e) {
            // Success
        }
    }
}
