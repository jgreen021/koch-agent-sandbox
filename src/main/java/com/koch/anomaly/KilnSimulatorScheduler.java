package com.koch.anomaly;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class KilnSimulatorScheduler {

    private static final Logger logger = LoggerFactory.getLogger(KilnSimulatorScheduler.class);

    private final KilnSensorProducer producer;
    private final KilnSimulationRegistry registry;
    private final Random random = new Random();

    private enum KilnState {
        NORMAL, WARNING, CRITICAL
    }

    // Store the time when the current state expires for each kiln
    private final Map<String, LocalDateTime> stateExpirationMap = new ConcurrentHashMap<>();
    private final Map<String, KilnState> currentStateMap = new ConcurrentHashMap<>();

    public KilnSimulatorScheduler(KilnSensorProducer producer, KilnSimulationRegistry registry) {
        this.producer = producer;
        this.registry = registry;
    }

    @Scheduled(fixedRateString = "${simulation.rate:2000}")
    public void simulateReadings() {
        LocalDateTime now = LocalDateTime.now();

        // Dynamically iterate over all active kilns in the registry
        // If the cache is empty (e.g. at startup with no DB records), no simulation occurs.
        registry.getCache().forEach((uuid, entry) -> {
            Kiln kiln = entry.kiln;
            String assetId = kiln.getName(); // Use the database name as the telemetry ID

            // Initialize state if not present
            currentStateMap.putIfAbsent(assetId, KilnState.NORMAL);
            stateExpirationMap.putIfAbsent(assetId, now.plusMinutes(5));

            // State transition logic
            if (now.isAfter(stateExpirationMap.get(assetId))) {
                double chance = random.nextDouble();
                double warningProb = kiln.getWarningProbability();
                double criticalProb = kiln.getCriticalProbability();
                double normalProb = 1.0 - warningProb - criticalProb;

                if (chance < normalProb) {
                    currentStateMap.put(assetId, KilnState.NORMAL);
                } else if (chance < (normalProb + warningProb)) {
                    currentStateMap.put(assetId, KilnState.WARNING);
                } else {
                    currentStateMap.put(assetId, KilnState.CRITICAL);
                }
                
                // Keep the state for the configured duration (seconds) or default to 5m
                int duration = kiln.getStateDurationSeconds() > 0 ? kiln.getStateDurationSeconds() : 300;
                stateExpirationMap.put(assetId, now.plusSeconds(duration));
                logger.info("Dynamic Kiln {} transitioned to {}", assetId, currentStateMap.get(assetId));
            }

            KilnState state = currentStateMap.get(assetId);

            // Use the kiln's specific thresholds from the database
            double baseTemp = switch (state) {
                case CRITICAL -> kiln.getCriticalTemp();
                case WARNING -> kiln.getWarningTemp();
                default -> kiln.getBaselineTemp();
            };

            // Add some noise (+/- 5 degrees)
            double tempVariation = (random.nextDouble() * 10) - 5;
            double currentTemp = baseTemp + tempVariation;

            AssetSensorReading reading = new AssetSensorReading(
                    null,
                    assetId,
                    "TEMPERATURE",
                    Math.round(currentTemp * 100.0) / 100.0,
                    "C",
                    now,
                    state.name()
            );

            logger.debug("Simulating dynamic reading for {}: {}", assetId, reading);
            producer.publishReading(reading);
        });
    }
}
