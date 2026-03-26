package com.koch.anomaly;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class KilnSensorConsumer {

    private static final Logger logger = LoggerFactory.getLogger(KilnSensorConsumer.class);

    @Autowired
    private AnomalyValidationService service;

    @Autowired
    private ObjectMapper objectMapper;

    @KafkaListener(topics = "kiln-sensor-readings", groupId = "edge-validation-group")
    public void consume(String payload) {
        AnomalyReading reading = null;
        try {
            // First attempt to parse as a JSON object
            reading = objectMapper.readValue(payload, AnomalyReading.class);
        } catch (Exception e) {
            // Fall back to simple double parsing if not valid JSON
            try {
                double parsedValue = Double.parseDouble(payload);
                reading = new AnomalyReading("KILN-01", parsedValue);
            } catch (NumberFormatException nfe) {
                logger.error("Failed to parse payload as JSON or double: [{}]", payload);
                return;
            }
        }

        AnomalyStatus status = service.isAnomaly(reading);

        if (status == AnomalyStatus.NORMAL) {
            logger.info("Reading OK: {}", reading.readingValue());
        } else if (status == AnomalyStatus.WARNING || status == AnomalyStatus.CRITICAL) {
            logger.warn("ALARM: {} - Value: {}", status, reading.readingValue());
        }
    }
}
