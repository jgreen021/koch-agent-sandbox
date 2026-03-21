package com.koch.anomaly;

import org.springframework.stereotype.Service;

@Service
public class AnomalyValidationService {

    private final AnomalyReadingRepository repository;

    public AnomalyValidationService(AnomalyReadingRepository repository) {
        this.repository = repository;
    }

    public boolean isAnomaly(AnomalyReading reading) {
        java.util.List<AnomalyReading> history = repository.findTop10ByAssetIdOrderByTimestampDesc(reading.getAssetId());

        if (history == null || history.size() < 10) {
            return false;
        }

        double sum = 0.0;
        for (AnomalyReading r : history) {
            sum += r.getReadingValue();
        }
        
        double average = sum / history.size();

        if (average == 0.0) {
            return reading.getReadingValue() != 0.0;
        }

        double deviationPercentage = Math.abs(reading.getReadingValue() - average) / average;
        
        return deviationPercentage > 0.20;
    }
}
