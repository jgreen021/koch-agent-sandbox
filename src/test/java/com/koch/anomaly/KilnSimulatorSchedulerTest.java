package com.koch.anomaly;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

class KilnSimulatorSchedulerTest {

    @Mock
    private KilnSensorProducer producer;

    private KilnSimulatorScheduler scheduler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        scheduler = new KilnSimulatorScheduler(producer);
    }

    @Test
    void testSimulateReadings_GeneratesAndPublishesData() {
        // Execute the scheduled method
        scheduler.simulateReadings();

        // Verify that the producer was called
        ArgumentCaptor<AssetSensorReading> captor = ArgumentCaptor.forClass(AssetSensorReading.class);
        verify(producer, atLeastOnce()).publishReading(captor.capture());

        // Ensure we got valid dummy data
        AssetSensorReading generated = captor.getValue();
        assertTrue(generated.assetId().startsWith("KILN-"));
        assertTrue(generated.readingValue() > 0);
    }
}
