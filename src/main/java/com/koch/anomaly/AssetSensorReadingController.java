package com.koch.anomaly;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;

import static com.koch.anomaly.SpecificationFactory.equalTo;
import static com.koch.anomaly.SpecificationFactory.greaterThanOrEqualTo;
import static com.koch.anomaly.SpecificationFactory.lessThanOrEqualTo;

@RestController
@RequestMapping("/api/sensors")
public class AssetSensorReadingController {

    private final AssetSensorReadingRepository repository;
    private final KilnSensorProducer producer;
    private final SseConnectionManager sseConnectionManager;

    public AssetSensorReadingController(AssetSensorReadingRepository repository, KilnSensorProducer producer, SseConnectionManager sseConnectionManager) {
        this.repository = repository;
        this.producer = producer;
        this.sseConnectionManager = sseConnectionManager;
    }

    @PostMapping("/readings")
    @ResponseStatus(HttpStatus.CREATED)
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('ROLE_OPERATOR')")
    public void postReading(@jakarta.validation.Valid @RequestBody AssetSensorReading reading) {
        producer.publishReading(reading);
    }

    @GetMapping("/readings")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyAuthority('ROLE_OPERATOR', 'ROLE_AUDITOR', 'ROLE_GATEWAY_ADMIN')")
    public ResponseEntity<Page<AssetSensorReading>> getReadings(
            Pageable pageable,
            @RequestParam(required = false) String assetId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String sensorType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime timestamp,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(required = false) Double readingValue,
            @RequestParam(required = false) Double minReadingValue,
            @RequestParam(required = false) Double maxReadingValue,
            @RequestParam(required = false) Integer readingId) {

        // Build specifications based on optional query parameters
        Specification<AssetSensorReadingEntity> spec = Specification.where(
                SpecificationFactory.<AssetSensorReadingEntity, String>equalTo("assetId", assetId))
                .and(equalTo("status", status))
                .and(equalTo("sensorType", sensorType))
                .and(equalTo("timestamp", timestamp))
                .and(greaterThanOrEqualTo("timestamp", startTime))
                .and(lessThanOrEqualTo("timestamp", endTime))
                .and(equalTo("readingValue", readingValue))
                .and(greaterThanOrEqualTo("readingValue", minReadingValue))
                .and(lessThanOrEqualTo("readingValue", maxReadingValue))
                .and(equalTo("readingId", readingId));

        Page<AssetSensorReading> readings = repository.findAll(spec, pageable)
                .map(AssetSensorReading::fromEntity);

        return ResponseEntity.ok(readings);
    }

    @GetMapping(value = "/stream/{assetId}", produces = org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE)
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyAuthority('ROLE_OPERATOR', 'ROLE_AUDITOR', 'ROLE_GATEWAY_ADMIN')")
    public SseEmitter streamReadings(@PathVariable String assetId) {
        return sseConnectionManager.register(assetId);
    }
}
