package com.koch.anomaly;

import java.time.LocalDateTime;

/**
 * Immutable Data Transfer Object representing a sensor reading.
 * Adheres to the Functional Programming paradigm specified in openspec/project.md.
 */
public record AssetSensorReading(
    Integer readingId,
    String assetId,
    String sensorType,
    Double readingValue,
    String uom,
    LocalDateTime timestamp,
    String status
) {
    public static AssetSensorReading fromEntity(AssetSensorReadingEntity entity) {
        return new AssetSensorReading(
                entity.getReadingId(),
                entity.getAssetId(),
                entity.getSensorType(),
                entity.getReadingValue(),
                entity.getUom(),
                entity.getTimestamp(),
                entity.getStatus()
        );
    }

    public AssetSensorReadingEntity toEntity() {
        AssetSensorReadingEntity entity = new AssetSensorReadingEntity();
        entity.setReadingId(this.readingId());
        entity.setAssetId(this.assetId());
        entity.setSensorType(this.sensorType());
        entity.setReadingValue(this.readingValue());
        entity.setUom(this.uom());
        entity.setTimestamp(this.timestamp());
        entity.setStatus(this.status());
        return entity;
    }
}
