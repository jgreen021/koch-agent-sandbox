package com.koch.anomaly;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "asset_sensor_readings")
public class AssetSensorReadingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ReadingID")
    private Integer readingId;

    @Column(name = "AssetID", length = 50, nullable = false)
    private String assetId;

    @Column(name = "SensorType", length = 30, nullable = false)
    private String sensorType;

    @Column(name = "ReadingValue", nullable = false)
    private Double readingValue;

    @Column(name = "UOM", length = 10)
    private String uom;

    @Column(name = "Timestamp")
    private LocalDateTime timestamp;

    @Column(name = "Status", length = 20)
    private String status;

    public Integer getReadingId() {
        return readingId;
    }

    public void setReadingId(Integer readingId) {
        this.readingId = readingId;
    }

    public String getAssetId() {
        return assetId;
    }

    public void setAssetId(String assetId) {
        this.assetId = assetId;
    }

    public String getSensorType() {
        return sensorType;
    }

    public void setSensorType(String sensorType) {
        this.sensorType = sensorType;
    }

    public Double getReadingValue() {
        return readingValue;
    }

    public void setReadingValue(Double readingValue) {
        this.readingValue = readingValue;
    }

    public String getUom() {
        return uom;
    }

    public void setUom(String uom) {
        this.uom = uom;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
