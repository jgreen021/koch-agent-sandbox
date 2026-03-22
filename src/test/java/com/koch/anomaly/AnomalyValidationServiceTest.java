package com.koch.anomaly;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AnomalyValidationServiceTest {

    @Test
    void testEvaluate_ColdStartRule_FewerThan10Readings() {
        AnomalyValidationService validationService = new AnomalyValidationService();
        String assetId = "ASSET-1";
        
        primeHistory(validationService, assetId, 1, 100.0);

        assertEquals(AnomalyStatus.INSUFFICIENT_DATA, validationService.isAnomaly(new AnomalyReading(assetId, 110.0)),
                "Fewer than 10 readings must explicitly return Insufficient Data");
    }

    @Test
    void testEvaluate_AbsoluteCeilingOverride_Exceeds120() {
        AnomalyValidationService validationService = new AnomalyValidationService();
        String assetId = "ASSET-2";
        
        primeHistory(validationService, assetId, 10, 120.0);

        assertEquals(AnomalyStatus.CRITICAL, validationService.isAnomaly(new AnomalyReading(assetId, 120.1)),
                "Any reading > 120.0 must be immediately classified as Critical");
    }

    @Test
    void testEvaluate_WarningTier_Between15And24_9Percent() {
        AnomalyValidationService service1 = new AnomalyValidationService();
        primeHistory(service1, "A1", 10, 90.0);
        assertEquals(AnomalyStatus.WARNING, service1.isAnomaly(new AnomalyReading("A1", 110.0)),
                "15% deviation should be flagged as Warning");

        AnomalyValidationService service2 = new AnomalyValidationService();
        primeHistory(service2, "A2", 10, 90.0);
        assertEquals(AnomalyStatus.WARNING, service2.isAnomaly(new AnomalyReading("A2", 112.41)),
                "24.9% deviation should be flagged as Warning");

        AnomalyValidationService service3 = new AnomalyValidationService();
        primeHistory(service3, "A3", 10, 90.0);
        assertEquals(AnomalyStatus.WARNING, service3.isAnomaly(new AnomalyReading("A3", 76.5)),
                "15% negative deviation should be flagged as Warning");
    }

    @Test
    void testEvaluate_CriticalTier_25PercentOrMore() {
        AnomalyValidationService service1 = new AnomalyValidationService();
        primeHistory(service1, "B1", 10, 80.0);
        assertEquals(AnomalyStatus.CRITICAL, service1.isAnomaly(new AnomalyReading("B1", 100.0)),
                "Exactly 25% deviation should be Critical");

        AnomalyValidationService service2 = new AnomalyValidationService();
        primeHistory(service2, "B2", 10, 80.0);
        assertEquals(AnomalyStatus.CRITICAL, service2.isAnomaly(new AnomalyReading("B2", 110.0)),
                "Deviation > 25% should be Critical");
    }

    @Test
    void testKafkaDispatchOnCritical_UsesJacksonSerialization() throws Exception {
        AnomalyValidationService service = new AnomalyValidationService();
        
        @SuppressWarnings("unchecked")
        KafkaTemplate<String, String> mockKafkaTemplate = Mockito.mock(KafkaTemplate.class);
        ObjectMapper objectMapper = new ObjectMapper();
        
        ReflectionTestUtils.setField(service, "kafkaTemplate", mockKafkaTemplate);
        ReflectionTestUtils.setField(service, "objectMapper", objectMapper);
        
        String assetId = "ASSET-KAFKA";
        primeHistory(service, assetId, 10, 100.0);
        
        // This evaluates to CRITICAL and triggers a Kafka send
        service.isAnomaly(new AnomalyReading(assetId, 126.0));
        
        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(mockKafkaTemplate).send(
            Mockito.eq("active-alarms"), 
            Mockito.eq(assetId), 
            payloadCaptor.capture()
        );
        
        String sentJson = payloadCaptor.getValue();
        AnomalyReading deserialized = objectMapper.readValue(sentJson, AnomalyReading.class);
        
        // Format-agnostic checking!
        assertEquals(assetId, deserialized.getAssetId());
        assertEquals(126.0, deserialized.getReadingValue());
    }

    private void primeHistory(AnomalyValidationService service, String assetId, int count, double value) {
        for (int i = 0; i < count; i++) {
            service.isAnomaly(new AnomalyReading(assetId, value));
        }
    }
}
