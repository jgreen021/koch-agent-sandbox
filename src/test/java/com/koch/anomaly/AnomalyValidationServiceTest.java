package com.koch.anomaly;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AnomalyValidationServiceTest {

    @Mock
    private AnomalyReadingRepository repository;

    @InjectMocks
    private AnomalyValidationService validationService;

    @Test
    void testEvaluate_ColdStartRule_FewerThan10Readings() {
        String assetId = "ASSET-1";
        // Setup repository to return only 1 reading
        when(repository.findTop10ByAssetIdOrderByTimestampDesc(assetId))
                .thenReturn(generateMockHistory(assetId, 1, 100.0));

        // Expected: INSUFFICIENT_DATA
        assertEquals(AnomalyStatus.INSUFFICIENT_DATA, validationService.isAnomaly(new AnomalyReading(assetId, 110.0)),
                "Fewer than 10 readings must explicitly return Insufficient Data");
    }

    @Test
    void testEvaluate_AbsoluteCeilingOverride_Exceeds120() {
        String assetId = "ASSET-1";
        // Setup average = 120.0 so the mathematical deviation is basically 0%
        List<AnomalyReading> history = generateMockHistory(assetId, 10, 120.0);
        when(repository.findTop10ByAssetIdOrderByTimestampDesc(assetId)).thenReturn(history);

        // Reading exactly 120.1 EXCEEDS 120.0 absolute ceiling -> Should map to CRITICAL
        // (Even though deviation from 120.0 is practically 0% and well below the Warning/Critical threshold percentage)
        assertEquals(AnomalyStatus.CRITICAL, validationService.isAnomaly(new AnomalyReading(assetId, 120.1)),
                "Any reading > 120.0 must be immediately classified as Critical, overriding other logic");
    }

    @Test
    void testEvaluate_WarningTier_Between15And24_9Percent() {
        String assetId = "ASSET-1";
        // Setup baseline average = 90.0
        List<AnomalyReading> history = generateMockHistory(assetId, 10, 90.0);
        when(repository.findTop10ByAssetIdOrderByTimestampDesc(assetId)).thenReturn(history);

        // Lower bound of Warning: exactly 15% deviation
        assertEquals(AnomalyStatus.WARNING, validationService.isAnomaly(new AnomalyReading(assetId, 110.0)),
                "15% deviation should be flagged as Warning");

        // Upper bound of Warning: exactly 24.9% deviation
        assertEquals(AnomalyStatus.WARNING, validationService.isAnomaly(new AnomalyReading(assetId, 112.41)),
                "24.9% deviation should be flagged as Warning");

        // Lower deviation exact boundary
        assertEquals(AnomalyStatus.WARNING, validationService.isAnomaly(new AnomalyReading(assetId, 76.5)),
                "15% negative deviation should be flagged as Warning");
    }

    @Test
    void testEvaluate_CriticalTier_25PercentOrMore() {
        String assetId = "ASSET-1";
        // Setup average = 80.0 (using 80.0 avoids clashing with the 120.0 Absolute Ceiling rule!)
        List<AnomalyReading> history = generateMockHistory(assetId, 10, 80.0);
        when(repository.findTop10ByAssetIdOrderByTimestampDesc(assetId)).thenReturn(history);

        // 25% of 80 is 20. Exact 25% deviation (80 + 20 = 100)
        assertEquals(AnomalyStatus.CRITICAL, validationService.isAnomaly(new AnomalyReading(assetId, 100.0)),
                "Exactly 25% deviation should be Critical");

        // Greater than 25%
        assertEquals(AnomalyStatus.CRITICAL, validationService.isAnomaly(new AnomalyReading(assetId, 110.0)),
                "Deviation > 25% should be Critical");
    }

    private List<AnomalyReading> generateMockHistory(String assetId, int count, double value) {
        return Collections.nCopies(count, new AnomalyReading(assetId, value));
    }
}
