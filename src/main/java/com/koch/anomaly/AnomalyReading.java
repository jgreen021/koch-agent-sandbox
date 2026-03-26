package com.koch.anomaly;

public record AnomalyReading(
    String assetId,
    double readingValue,
    String uom,
    String sensorType
) {
    public AnomalyReading(String assetId, double readingValue) {
        this(assetId, readingValue, null, null);
    }

    public AnomalyReading(String assetId, double readingValue, String sensorType) {
        this(assetId, readingValue, null, sensorType);
    }
}
