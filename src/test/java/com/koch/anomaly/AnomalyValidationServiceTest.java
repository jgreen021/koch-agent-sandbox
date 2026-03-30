package com.koch.anomaly;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AnomalyValidationServiceTest {

    private AnomalyValidationService validationService;
    private AssetSensorReadingRepository mockRepository;
    private KafkaTemplate<String, String> mockKafkaTemplate;
    private com.koch.security.AuditService mockAuditService;
    private ObjectMapper objectMapper;

    @BeforeEach
    @SuppressWarnings("unchecked")
    public void setUp() {
        validationService = new AnomalyValidationService();
        mockRepository = Mockito.mock(AssetSensorReadingRepository.class);
        mockKafkaTemplate = Mockito.mock(KafkaTemplate.class);
        mockAuditService = Mockito.mock(com.koch.security.AuditService.class);
        objectMapper = new ObjectMapper();

        ReflectionTestUtils.setField(validationService, "repository", mockRepository);
        ReflectionTestUtils.setField(validationService, "kafkaTemplate", mockKafkaTemplate);
        ReflectionTestUtils.setField(validationService, "auditService", mockAuditService);
        ReflectionTestUtils.setField(validationService, "objectMapper", objectMapper);

        // Set default thresholds for tests
        ReflectionTestUtils.setField(validationService, "criticalThreshold", 120.0);
        ReflectionTestUtils.setField(validationService, "criticalDeviation", 0.25);
        ReflectionTestUtils.setField(validationService, "warningDeviation", 0.15);
        ReflectionTestUtils.setField(validationService, "historySize", 10);
    }

    @Test
    void testEvaluate_ColdStartRule_FewerThan10Readings() {
        String assetId = "ASSET-1";

        primeHistory(validationService, assetId, 1, 100.0);

        assertEquals(AnomalyStatus.INSUFFICIENT_DATA, validationService.isAnomaly(new AnomalyReading(assetId, 110.0)),
                "Fewer than 10 readings must explicitly return Insufficient Data");
    }

    @Test
    void testEvaluate_AbsoluteCeilingOverride_Exceeds120() {
        String assetId = "ASSET-2";

        primeHistory(validationService, assetId, 10, 120.0);

        assertEquals(AnomalyStatus.CRITICAL, validationService.isAnomaly(new AnomalyReading(assetId, 120.1)),
                "Any reading > 120.0 must be immediately classified as Critical");
    }

    @Test
    void testEvaluate_WarningTier_Between15And24_9Percent() {
        primeHistory(validationService, "A1", 10, 90.0);
        assertEquals(AnomalyStatus.WARNING, validationService.isAnomaly(new AnomalyReading("A1", 110.0)),
                "15% deviation should be flagged as Warning");
    }

    @Test
    void testEvaluate_CriticalTier_25PercentOrMore() {
        primeHistory(validationService, "B1", 10, 80.0);
        assertEquals(AnomalyStatus.CRITICAL, validationService.isAnomaly(new AnomalyReading("B1", 100.0)),
                "Exactly 25% deviation should be Critical");
    }

    @Test
    void testKafkaDispatchOnCritical_UsesJacksonSerialization() throws Exception {
        String assetId = "ASSET-KAFKA";
        primeHistory(validationService, assetId, 10, 100.0);

        // This evaluates to CRITICAL and triggers a Kafka send
        validationService.isAnomaly(new AnomalyReading(assetId, 126.0));

        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(mockKafkaTemplate).send(
                Mockito.eq("active-alarms"),
                Mockito.eq(assetId),
                payloadCaptor.capture()
        );

        String sentJson = payloadCaptor.getValue();
        AnomalyReading deserialized = objectMapper.readValue(sentJson, AnomalyReading.class);

        // Format-agnostic checking!
        assertEquals(assetId, deserialized.assetId());
        assertEquals(126.0, deserialized.readingValue());
    }

    @Test
    void testRepositorySave_OnlyCalledOnCritical() {
        String assetId = "ASSET-REPO-SAVE";
        primeHistory(validationService, assetId, 10, 100.0);
        // Bypass testing the priming calls
        Mockito.clearInvocations(mockRepository);

        // Warning trigger (15% diff) - Should NOT save anymore
        validationService.isAnomaly(new AnomalyReading(assetId, 115.0));
        verify(mockRepository, never()).save(any(AssetSensorReadingEntity.class));

        Mockito.clearInvocations(mockRepository);

        // Critical trigger (25% diff) - Should save
        validationService.isAnomaly(new AnomalyReading(assetId, 125.0));
        verify(mockRepository, Mockito.times(1)).save(any(AssetSensorReadingEntity.class));
    }

    @Test
    void testRepositorySave_NeverCalledOnOkOrInsufficient() {
        String assetId = "ASSET-REPO-SKIP";
        Mockito.clearInvocations(mockRepository);

        // Insufficient Data
        validationService.isAnomaly(new AnomalyReading(assetId, 100.0));
        verify(mockRepository, never()).save(any(AssetSensorReadingEntity.class));

        primeHistory(validationService, assetId, 9, 100.0); // getting history size to 10
        Mockito.clearInvocations(mockRepository);

        // OK trigger (0% diff)
        validationService.isAnomaly(new AnomalyReading(assetId, 100.0));
        verify(mockRepository, never()).save(any(AssetSensorReadingEntity.class));
    }

    @Test
    void testRepositorySave_CapturesSensorType() {
        String assetId = "SENS-TYPE-TEST";
        String sensorType = "VIBRATION";
        primeHistory(validationService, assetId, 10, 100.0);

        // Critical trigger (Reading > 120)
        validationService.isAnomaly(new AnomalyReading(assetId, 130.0, sensorType));

        ArgumentCaptor<AssetSensorReadingEntity> entityCaptor = ArgumentCaptor.forClass(AssetSensorReadingEntity.class);
        verify(mockRepository).save(entityCaptor.capture());

        assertEquals(sensorType, entityCaptor.getValue().getSensorType(),
                "The saved entity must contain the sensorType from the original reading");
    }

    private void primeHistory(AnomalyValidationService service, String assetId, int count, double value) {
        for (int i = 0; i < count; i++) {
            service.isAnomaly(new AnomalyReading(assetId, value));
        }
    }
}
