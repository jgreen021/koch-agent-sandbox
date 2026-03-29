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
    private final Random random = new Random();

    private enum KilnState {
        NORMAL, WARNING, CRITICAL
    }

    // Store the time when the current state expires for each kiln
    private final Map<String, LocalDateTime> stateExpirationMap = new ConcurrentHashMap<>();
    private final Map<String, KilnState> currentStateMap = new ConcurrentHashMap<>();

    public KilnSimulatorScheduler(KilnSensorProducer producer) {
        this.producer = producer;
        // Initialize states
        for (int i = 1; i <= 3; i++) {
            String assetId = "KILN-0" + i;
            currentStateMap.put(assetId, KilnState.NORMAL);
            stateExpirationMap.put(assetId, LocalDateTime.now().plusMinutes(10));
        }
    }

    @Scheduled(fixedRateString = "${simulation.rate:2000}")
    public void simulateReadings() {
        LocalDateTime now = LocalDateTime.now();

        for (int i = 1; i <= 3; i++) {
            String assetId = "KILN-0" + i;

            // State transition logic
            if (now.isAfter(stateExpirationMap.get(assetId))) {
                double chance = random.nextDouble();
                if (chance < 0.8) {
                    currentStateMap.put(assetId, KilnState.NORMAL);
                } else if (chance < 0.95) {
                    currentStateMap.put(assetId, KilnState.WARNING);
                } else {
                    currentStateMap.put(assetId, KilnState.CRITICAL);
                }
                // Keep the state for 10 minutes
                stateExpirationMap.put(assetId, now.plusMinutes(3));
                logger.info("Kiln {} transitioned to {}", assetId, currentStateMap.get(assetId));
            }

            KilnState state = currentStateMap.get(assetId);

            double baseTemp = switch (state) {
                case CRITICAL ->
                    310.0;
                case WARNING ->
                    265.0;
                default ->
                    200.0;
            };

            // Add some noise (+/- 10 degrees)
            double tempVariation = (random.nextDouble() * 20) - 10;
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

            logger.debug("Simulating reading: {}", reading);
            producer.publishReading(reading);
        }
    }
}
