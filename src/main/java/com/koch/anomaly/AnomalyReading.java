package com.koch.anomaly;

public class AnomalyReading {

    private String assetId;
    private double readingValue;

    public AnomalyReading() {
    }

    public AnomalyReading(String assetId, double readingValue) {
        this.assetId = assetId;
        this.readingValue = readingValue;
    }

    public String getAssetId() {
        return assetId;
    }

    public void setAssetId(String assetId) {
        this.assetId = assetId;
    }

    public double getReadingValue() {
        return readingValue;
    }

    public void setReadingValue(double readingValue) {
        this.readingValue = readingValue;
    }
}
