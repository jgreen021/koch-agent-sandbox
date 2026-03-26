package com.koch.anomaly;

public class AnomalyReading {

    private String assetId;
    private double readingValue;
    private String uom;
    private String sensorType;

    public AnomalyReading() {
    }

    public AnomalyReading(String assetId, double readingValue) {
        this.assetId = assetId;
        this.readingValue = readingValue;
    }

    public AnomalyReading(String assetId, double readingValue, String sensorType) {
        this.assetId = assetId;
        this.readingValue = readingValue;
        this.sensorType = sensorType;
    }

    public AnomalyReading(String assetId, double readingValue, String uom, String sensorType) {
        this.assetId = assetId;
        this.readingValue = readingValue;
        this.uom = uom;
        this.sensorType = sensorType;
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

    public String getSensorType() {
        return sensorType;
    }

    public void setSensorType(String sensorType) {
        this.sensorType = sensorType;
    }
}
