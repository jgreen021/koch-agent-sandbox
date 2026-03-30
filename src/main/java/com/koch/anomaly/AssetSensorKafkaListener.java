package com.koch.anomaly;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class AssetSensorKafkaListener {

    private static final Logger logger = LoggerFactory.getLogger(AssetSensorKafkaListener.class);
    
    private final SseConnectionManager sseConnectionManager;
    private final AssetSensorReadingRepository repository;
    private final org.springframework.kafka.core.KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public AssetSensorKafkaListener(SseConnectionManager sseConnectionManager, 
                                   AssetSensorReadingRepository repository,
                                   org.springframework.kafka.core.KafkaTemplate<String, String> kafkaTemplate,
                                   ObjectMapper objectMapper) {
        this.sseConnectionManager = sseConnectionManager;
        this.repository = repository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    private final java.util.Map<String, Integer> retryCounts = new java.util.concurrent.ConcurrentHashMap<>();

    @KafkaListener(topics = "kiln-sensor-readings", groupId = "sse-dashboard-group")
    public void consumeReading(String payload) {
        logger.debug("Received reading from Kafka: {}", payload);
        try {
            AssetSensorReading reading = objectMapper.readValue(payload, AssetSensorReading.class);
            
            // Broadcast ALL readings immediately to keep the client UI live
            sseConnectionManager.broadcast(reading.assetId(), payload);

            // Per Project Standard: Reserve DB strictly for high-priority alerts (Critical)
            if ("CRITICAL".equals(reading.status())) {
                processCriticalReadingWithRetry(reading, payload);
            }
            // Clear retry count on success
            retryCounts.remove(payload);
        } catch (JsonProcessingException e) {
            logger.error("Failed to parse telemetry reading JSON - permanently discarding", e);
        } catch (Exception e) {
            handleProcessingFailure(payload, e);
        }
    }

    private void processCriticalReadingWithRetry(AssetSensorReading reading, String payload) throws Exception {
        try {
            repository.save(reading.toEntity());
            
            // Also forward to the persistent Active Alarms topic for external notifications
            kafkaTemplate.send("active-alarms", reading.assetId(), payload);
            logger.info("CRITICAL ALARM forwarded to 'active-alarms' topic for asset: {}", reading.assetId());
        } catch (Exception e) {
            // Rethrow to trigger retry logic
            throw e;
        }
    }

    private void handleProcessingFailure(String payload, Exception e) {
        int attempts = retryCounts.getOrDefault(payload, 0) + 1;
        if (attempts < 3) {
            retryCounts.put(payload, attempts);
            logger.warn("Processing failed (Attempt {}/3). Retrying... Error: {}", attempts, e.getMessage());
            // In a real app, we might use a delayed retry. Here we just log and wait for the next poll or 
            // re-trigger. For this sandbox, we'll just re-push to the end of the topic to simulate a retry.
            kafkaTemplate.send("kiln-sensor-readings", payload);
        } else {
            retryCounts.remove(payload);
            logger.error("Processing failed after 3 attempts. Moving to DLQ. Error: {}", e.getMessage());
            kafkaTemplate.send("kiln-sensor-readings-dlq", payload);
        }
    }
}
