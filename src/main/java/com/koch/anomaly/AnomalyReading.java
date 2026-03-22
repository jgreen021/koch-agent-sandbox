package com.koch.anomaly;

public class AnomalyReading {

    private String assetId;
    private double readingValue;
    private String uom;

    public AnomalyReading() {
    }

    public AnomalyReading(String assetId, double readingValue) {
        this.assetId = assetId;
        this.readingValue = readingValue;
    }

    public AnomalyReading(String assetId, double readingValue, String uom) {
        this.assetId = assetId;
        this.readingValue = readingValue;
        this.uom = uom;
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

    public String getUom() {
        return uom;
    }

    public void setUom(String uom) {
        this.uom = uom;
    }
}
