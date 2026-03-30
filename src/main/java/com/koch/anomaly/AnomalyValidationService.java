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
    
    @org.springframework.beans.factory.annotation.Value("${app.anomaly.critical-threshold:120.0}")
    private double criticalThreshold;

    @org.springframework.beans.factory.annotation.Value("${app.anomaly.critical-deviation:0.25}")
    private double criticalDeviation;

    @org.springframework.beans.factory.annotation.Value("${app.anomaly.warning-deviation:0.15}")
    private double warningDeviation;

    @org.springframework.beans.factory.annotation.Value("${app.anomaly.history-size:10}")
    private int historySize;

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @jakarta.annotation.PostConstruct
    public void init() {
        logger.info("AnomalyValidationService started. Connection URL: {}", env.getProperty("spring.datasource.url"));
        try {
            // Updated for Oracle compatibility
            String instanceName = jdbcTemplate.queryForObject("SELECT sys_context('USERENV', 'INSTANCE_NAME') FROM dual", String.class);
            String dbName = jdbcTemplate.queryForObject("SELECT sys_context('USERENV', 'DB_NAME') FROM dual", String.class);
            logger.info("Connected to Oracle Instance: {} - DB: {}", instanceName, dbName);
        } catch (org.springframework.dao.DataAccessException e) {
            logger.error("Failed to fetch Oracle metadata via JDBC", e);
        }
    }

    @Autowired(required = false)
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AssetSensorReadingRepository repository;

    @Autowired
    private com.koch.security.AuditService auditService;

    private final com.google.common.cache.LoadingCache<String, List<AnomalyReading>> readingHistory = 
        com.google.common.cache.CacheBuilder.newBuilder()
            .maximumSize(10000)
            .expireAfterAccess(24, java.util.concurrent.TimeUnit.HOURS)
            .build(new com.google.common.cache.CacheLoader<>() {
                @Override
                public List<AnomalyReading> load(String key) {
                    return new java.util.concurrent.CopyOnWriteArrayList<>();
                }
            });

    @org.springframework.transaction.annotation.Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public AnomalyStatus isAnomaly(AnomalyReading reading) {
        String assetId = reading.assetId();
        List<AnomalyReading> history;
        try {
            history = readingHistory.get(assetId);
        } catch (java.util.concurrent.ExecutionException e) {
            logger.error("Failed to load reading history from cache for asset: {}", assetId, e);
            history = new java.util.concurrent.CopyOnWriteArrayList<>();
        }
        // If we don't have enough readings yet, add the current one and return INSUFFICIENT_DATA
        if (history.size() < historySize) {
            history.add(reading);
            return AnomalyStatus.INSUFFICIENT_DATA;
        }

        // At this point, history contains exactly 10 readings (the previous 10).
        // Calculate average based on these 10 readings.
        double sum = 0.0;
        for (AnomalyReading r : history) {
            sum += r.readingValue();
        }

        double average = sum / history.size(); // history.size() is 10 here

        // Now, add the new reading and trim the history to keep only the last configured size.
        history.add(reading);
        if (history.size() > historySize) {
            history.remove(0); // Remove the oldest reading
        }

        AnomalyStatus result = AnomalyStatus.NORMAL;

        if (average == 0.0) {
            // Safe fallback if average is somehow 0
            if (reading.readingValue() > criticalThreshold) {
                result = AnomalyStatus.CRITICAL;
            }
        } else {
            double deviationPercentage = Math.abs(reading.readingValue() - average) / average;

            if (reading.readingValue() > criticalThreshold) {
                result = AnomalyStatus.CRITICAL;
            } else if (deviationPercentage >= criticalDeviation) {
                result = AnomalyStatus.CRITICAL;
            } else if (deviationPercentage >= warningDeviation) {
                result = AnomalyStatus.WARNING;
            }
        }

        // Event Sourcing Light: Log detection event to Audit Log
        if (result == AnomalyStatus.CRITICAL || result == AnomalyStatus.WARNING) {
            String details = String.format("Asset: %s, Value: %.2f, Type: %s", 
                                          reading.assetId(), reading.readingValue(), reading.sensorType());
            auditService.logEvent("ANOMALY_" + result.name(), "SYSTEM", "/api/sensors/anomaly", details);
        }

        if (result == AnomalyStatus.CRITICAL) {
            AssetSensorReadingEntity entity = new AssetSensorReadingEntity();
            entity.setAssetId(reading.assetId());
            entity.setReadingValue(reading.readingValue());
            entity.setUom(reading.uom());
            entity.setSensorType(reading.sensorType() != null ? reading.sensorType() : "UNKNOWN");
            entity.setTimestamp(LocalDateTime.now());
            entity.setStatus(result.name());
            try {
                repository.save(entity);
                repository.flush();
                logger.info("ALARM: {} - Value: {} - Saved to DB. Current DB Count: {}", result, reading.readingValue(), repository.count());
            } catch (Exception e) {
                logger.error("Failed to save anomaly reading to DB", e);
            }
        }

        if (result == AnomalyStatus.CRITICAL && kafkaTemplate != null) {
            try {
                String payload = objectMapper.writeValueAsString(reading);
                kafkaTemplate.send("active-alarms", reading.assetId(), payload);
                logger.info("Forwarded CRITICAL alarm to active-alarms topic.");
            } catch (JsonProcessingException e) {
                logger.error("Failed to serialize reading to JSON", e);
            }
        }

        return result;
    }
}
