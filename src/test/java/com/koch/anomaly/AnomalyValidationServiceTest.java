package com.koch.anomaly;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AnomalyValidationServiceTest {

    // Note in TDD: These classes do not exist in the production source yet!
    @Mock
    private AnomalyReadingRepository repository;

    @InjectMocks
    private AnomalyValidationService validationService;

    @Test
    void testIsAnomaly_DeviationStrictlyGreaterThan20PercentHigher_ReturnsTrue() {
        // Arrange
        String assetId = "ASSET-99";
        
        // Setup 10 readings that uniformly average to a value of 100
        List<AnomalyReading> last10Readings = Arrays.asList(
            new AnomalyReading(assetId, 100.0), new AnomalyReading(assetId, 100.0),
            new AnomalyReading(assetId, 100.0), new AnomalyReading(assetId, 100.0),
            new AnomalyReading(assetId, 100.0), new AnomalyReading(assetId, 100.0),
            new AnomalyReading(assetId, 100.0), new AnomalyReading(assetId, 100.0),
            new AnomalyReading(assetId, 100.0), new AnomalyReading(assetId, 100.0)
        );

        when(repository.findTop10ByAssetIdOrderByTimestampDesc(assetId)).thenReturn(last10Readings);

        // Act
        // 120.0 is exactly 20% higher than the 100 average. (Should not be anomaly)
        boolean exactThreshold = validationService.isAnomaly(new AnomalyReading(assetId, 120.0));
        
        // 121.0 is > 20% higher than the 100 average. (Should be anomaly)
        boolean aboveThreshold = validationService.isAnomaly(new AnomalyReading(assetId, 121.0));

        // Assert
        assertFalse(exactThreshold, "A reading exactly 20% higher should NOT be flagged as an anomaly.");
        assertTrue(aboveThreshold, "A reading > 20% higher should be flagged as an anomaly.");
    }

    @Test
    void testIsAnomaly_DeviationStrictlyGreaterThan20PercentLower_ReturnsTrue() {
        // Arrange
        String assetId = "ASSET-99";
        
        // Setup 10 readings that average to 100
        List<AnomalyReading> last10Readings = Arrays.asList(
            new AnomalyReading(assetId, 100.0), new AnomalyReading(assetId, 100.0),
            new AnomalyReading(assetId, 100.0), new AnomalyReading(assetId, 100.0),
            new AnomalyReading(assetId, 100.0), new AnomalyReading(assetId, 100.0),
            new AnomalyReading(assetId, 100.0), new AnomalyReading(assetId, 100.0),
            new AnomalyReading(assetId, 100.0), new AnomalyReading(assetId, 100.0)
        );
        when(repository.findTop10ByAssetIdOrderByTimestampDesc(assetId)).thenReturn(last10Readings);

        // Act
        // 80.0 is exactly 20% lower.
        boolean exactThreshold = validationService.isAnomaly(new AnomalyReading(assetId, 80.0));
        
        // 79.0 is > 20% lower.
        boolean belowThreshold = validationService.isAnomaly(new AnomalyReading(assetId, 79.0));

        // Assert
        assertFalse(exactThreshold, "A reading exactly 20% lower should NOT be flagged as an anomaly.");
        assertTrue(belowThreshold, "A reading > 20% lower should be flagged as an anomaly.");
    }

    @Test
    void testIsAnomaly_LessThan10Readings_ReturnsFalse() {
        // Arrange
        String assetId = "ASSET-99";
        // Less than 10 readings available
        List<AnomalyReading> partialHistory = Arrays.asList(
            new AnomalyReading(assetId, 100.0), new AnomalyReading(assetId, 100.0)
        );
        when(repository.findTop10ByAssetIdOrderByTimestampDesc(assetId)).thenReturn(partialHistory);

        // Act
        // Even if wildly out of range (999.0), it shouldn't be flagged yet because baseline is insufficient.
        boolean result = validationService.isAnomaly(new AnomalyReading(assetId, 999.0));

        // Assert
        assertFalse(result, "Should return false if there are fewer than 10 historical readings to baseline against.");
    }
}
