package com.koch.anomaly;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class AnomalyValidationService {

    private static final Logger logger = LoggerFactory.getLogger(AnomalyValidationService.class);

    @Autowired
    private org.springframework.core.env.Environment env;

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @jakarta.annotation.PostConstruct
    public void init() {
        logger.info("AnomalyValidationService started. Connection URL: {}", env.getProperty("spring.datasource.url"));
        try {
            String serverName = jdbcTemplate.queryForObject("SELECT @@SERVERNAME", String.class);
            String dbName = jdbcTemplate.queryForObject("SELECT DB_NAME()", String.class);
            logger.info("Connected to Server: {} - DB: {}", serverName, dbName);
        } catch (Exception e) {
            logger.error("Failed to fetch server/db name from JDBC", e);
        }
    }

    @Autowired(required = false)
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AssetSensorReadingRepository repository;

    private final Map<String, List<AnomalyReading>> readingHistory = new ConcurrentHashMap<>();

    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public AnomalyStatus isAnomaly(AnomalyReading reading) {
        String assetId = reading.getAssetId();
        List<AnomalyReading> history = readingHistory.computeIfAbsent(assetId, k -> new CopyOnWriteArrayList<>());

        // If we don't have 10 readings yet, add the current one and return INSUFFICIENT_DATA
        if (history.size() < 10) {
            history.add(reading);
            return AnomalyStatus.INSUFFICIENT_DATA;
        }

        // At this point, history contains exactly 10 readings (the previous 10).
        // Calculate average based on these 10 readings.
        double sum = 0.0;
        for (AnomalyReading r : history) {
            sum += r.getReadingValue();
        }

        double average = sum / history.size(); // history.size() is 10 here

        // Now, add the new reading and trim the history to keep only the last 10.
        // This ensures that for the *next* call, 'history' will contain the current reading
        // and the 9 previous ones.
        history.add(reading);
        if (history.size() > 10) {
            history.remove(0); // Remove the oldest reading
        }

        AnomalyStatus result = AnomalyStatus.NORMAL;

        if (average == 0.0) {
            // Safe fallback if average is somehow 0
            if (reading.getReadingValue() > 120.0) {
                result = AnomalyStatus.CRITICAL;
            }
        } else {
            double deviationPercentage = Math.abs(reading.getReadingValue() - average) / average;

            if (reading.getReadingValue() > 120.0) {
                result = AnomalyStatus.CRITICAL;
            } else if (deviationPercentage >= 0.25) {
                result = AnomalyStatus.CRITICAL;
            } else if (deviationPercentage >= 0.15) {
                result = AnomalyStatus.WARNING;
            }
        }

        if (result == AnomalyStatus.CRITICAL || result == AnomalyStatus.WARNING) {
            AssetSensorReadingEntity entity = new AssetSensorReadingEntity();
            entity.setAssetId(reading.getAssetId());
            entity.setReadingValue(reading.getReadingValue());
            entity.setUom(reading.getUom());
            entity.setSensorType("UNKNOWN");
            entity.setTimestamp(LocalDateTime.now());
            entity.setStatus(result.name());
            try {
                repository.save(entity);
                repository.flush();
                logger.info("ALARM: {} - Value: {} - Saved to DB. Current DB Count: {}", result, reading.getReadingValue(), repository.count());
            } catch (Exception e) {
                logger.error("Failed to save anomaly reading to DB", e);
            }
        }

        if (result == AnomalyStatus.CRITICAL && kafkaTemplate != null) {
            try {
                String payload = objectMapper.writeValueAsString(reading);
                kafkaTemplate.send("active-alarms", reading.getAssetId(), payload);
                logger.info("Forwarded CRITICAL alarm to active-alarms topic.");
            } catch (JsonProcessingException e) {
                logger.error("Failed to serialize reading to JSON", e);
            }
        }

        return result;
    }
}
