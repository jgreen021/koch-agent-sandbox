package com.koch.anomaly;

import org.springframework.stereotype.Service;

@Service
public class AnomalyValidationService {

    private final AnomalyReadingRepository repository;

    public AnomalyValidationService(AnomalyReadingRepository repository) {
        this.repository = repository;
    }

    public AnomalyStatus isAnomaly(AnomalyReading reading) {
        java.util.List<AnomalyReading> history = repository.findTop10ByAssetIdOrderByTimestampDesc(reading.getAssetId());

        if (history == null || history.size() < 10) {
            return AnomalyStatus.INSUFFICIENT_DATA;
        }

        double sum = 0.0;
        for (AnomalyReading r : history) {
            sum += r.getReadingValue();
        }
        
        double average = sum / history.size();

        if (average == 0.0) {
            // Safe fallback if average is somehow 0
            if (reading.getReadingValue() > 120.0) return AnomalyStatus.CRITICAL;
            return AnomalyStatus.NORMAL;
        }

        double deviationPercentage = Math.abs(reading.getReadingValue() - average) / average;
        
        if (reading.getReadingValue() > 120.0) {
            return AnomalyStatus.CRITICAL;
        }

        if (deviationPercentage >= 0.25) {
            return AnomalyStatus.CRITICAL;
        }
        
        if (deviationPercentage >= 0.15) {
            return AnomalyStatus.WARNING;
        }

        return AnomalyStatus.NORMAL;
    }
}
