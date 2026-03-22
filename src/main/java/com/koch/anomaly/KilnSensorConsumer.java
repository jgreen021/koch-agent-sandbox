package com.koch.anomaly;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KilnSensorConsumer {

    private static final Logger logger = LoggerFactory.getLogger(KilnSensorConsumer.class);

    @Autowired
    private AnomalyValidationService service;

    @KafkaListener(topics = "kiln-sensor-readings", groupId = "edge-validation-group")
    public void consume(String payload) {
        try {
            double parsedValue = Double.parseDouble(payload);
            AnomalyReading reading = new AnomalyReading("KILN-01", parsedValue);
            AnomalyStatus status = service.isAnomaly(reading); // Called your method 'isAnomaly' corresponding to 'evaluateReading'

            if (status == AnomalyStatus.NORMAL) {
                logger.info("Reading OK: {}", parsedValue);
            } else if (status == AnomalyStatus.WARNING || status == AnomalyStatus.CRITICAL) {
                logger.warn("ALARM: {} - Value: {}", status, parsedValue);
            }
        } catch (NumberFormatException e) {
            logger.error("Failed to parse payload as double: [{}]", payload);
        }
    }
}
