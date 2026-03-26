package com.koch.anomaly;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KilnSensorProducer {

    private static final Logger logger = LoggerFactory.getLogger(KilnSensorProducer.class);
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public KilnSensorProducer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishReading(AssetSensorReading reading) {
        try {
            String payload = objectMapper.writeValueAsString(reading);
            logger.info("Publishing sensor reading to Kafka: {}", payload);
            kafkaTemplate.send("kiln-sensor-readings", payload);
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize AssetSensorReading: {}", reading, e);
            throw new RuntimeException("Serialization failure", e);
        }
    }
}
