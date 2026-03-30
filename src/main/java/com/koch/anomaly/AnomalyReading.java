package com.koch.anomaly;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AnomalyReading(
    @NotBlank(message = "Asset ID is required")
    String assetId,
    
    @Min(value = 0, message = "Reading value must be non-negative")
    double readingValue,
    
    String uom,
    
    @NotNull(message = "Sensor type is required")
    String sensorType
) {
    public AnomalyReading(String assetId, double readingValue) {
        this(assetId, readingValue, null, null);
    }

    public AnomalyReading(String assetId, double readingValue, String sensorType) {
        this(assetId, readingValue, null, sensorType);
    }
}
