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

    @KafkaListener(topics = "kiln-sensor-readings", groupId = "sse-dashboard-group")
    public void consumeReading(String payload) {
        logger.debug("Received reading from Kafka: {}", payload);
        try {
            AssetSensorReading reading = objectMapper.readValue(payload, AssetSensorReading.class);
            
            // Broadcast ALL readings immediately to keep the client UI live
            sseConnectionManager.broadcast(reading.assetId(), payload);

            // Per Project Standard: Reserve DB strictly for high-priority alerts (Critical)
            if ("CRITICAL".equals(reading.status())) {
                try {
                    repository.save(reading.toEntity());
                    
                    // Also forward to the persistent Active Alarms topic for external notifications
                    kafkaTemplate.send("active-alarms", reading.assetId(), payload);
                    logger.info("CRITICAL ALARM forwarded to 'active-alarms' topic for asset: {}", reading.assetId());
                    
                } catch (Exception dbEx) {
                    logger.error("Database persistence or alarm forwarding failed for critical alert, but client broadcast succeeded", dbEx);
                }
            }
        } catch (JsonProcessingException e) {
            logger.error("Failed to parse telemetry reading JSON", e);
        } catch (Exception e) {
            logger.error("Error processing telemetry reading", e);
        }
    }
}
