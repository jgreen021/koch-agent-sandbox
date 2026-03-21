package com.koch.anomaly;

public final class AnomalyReading {

    private final String assetId;
    private final double readingValue;

    public AnomalyReading(String assetId, double readingValue) {
        this.assetId = assetId;
        this.readingValue = readingValue;
    }

    public String getAssetId() {
        return assetId;
    }

    public double getReadingValue() {
        return readingValue;
    }
}
