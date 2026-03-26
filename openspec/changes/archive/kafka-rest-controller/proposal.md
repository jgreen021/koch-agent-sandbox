## Why

Currently, the system can consume and validate kiln sensor readings via `KilnSensorConsumer`, but it lacks a built-in mechanism to produce these events internally from the application's edge API. Integrating a Kafka Producer service will allow sensor data received via the REST API to be injected directly into the processing pipeline for real-time evaluation and persistence.

## What Changes

- **New Service**: Implement `KilnSensorProducer` to encapsulate the logic for sending `AssetSensorReading` payloads to the `kiln-sensor-readings` Kafka topic.
- **API Extension**: Add a JSON `POST` endpoint to `AssetSensorReadingController` that accepts a sensor reading and delegates to the producer service.
- **Kafka Configuration**: Update `application.yml` to define producer properties and the target bootstrap server for Kafka connectivity.

## Capabilities

### New Capabilities
- `sensor-production`: Standardized API and service layer for publishing industrial sensor data to Kafka topics.

### Modified Capabilities
- (None)

## Impact

- `AssetSensorReadingController.java`: Addition of `POST` endpoint for data injection.
- `KilnSensorProducer.java`: New class using `KafkaTemplate`.
- `application.yml`: Addition of `spring.kafka.producer` settings.
