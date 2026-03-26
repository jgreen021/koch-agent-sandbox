## Context

The system currently processes industrial sensor readings via a Kafka consumer (`KilnSensorConsumer`) and provides a read-only REST API via `AssetSensorReadingController`. However, there is no integrated way to ingest new sensor readings directly through the edge gateway or to perform manual data injection for testing. This design introduces a producer service to bridge the gap between the REST entry point and the Kafka messaging backbone.

## Goals / Non-Goals

**Goals:**
- Implement a Kafka producer service to send `AssetSensorReading` events.
- Extend the `AssetSensorReadingController` with a REST endpoint for data submission.
- Ensure all outgoing messages follow the immutable record pattern and Jackson-based serialization.

**Non-Goals:**
- Changing the existing `KilnSensorConsumer` processing or validation logic.
- Implementing complex retry or DLQ mechanisms for the producer (out of scope for initial feature).

## Decisions

- **KafkaTemplate usage**: We will use `KafkaTemplate<String, Object>` (or `AssetSensorReading`) to publish events. This aligns with Spring Kafka's best practices for Spring Boot applications.
- **REST Delegate Pattern**: The controller will not call the `KafkaTemplate` directly. Instead, it will delegate to a `KilnSensorProducer` service, promoting Single Responsibility and easier testing.
- **Topic Configuration**: The topic name `kiln-sensor-readings` will be externalized in `application.yml` to maintain flexibility across environments.

## Risks / Trade-offs

- **Risk: Infinite Loop** → If the producer sends data that the consumer then re-produces or triggers another API call. **Mitigation**: Ensure the circular path is intentional (testing) or use headers to identify origin.
- **Risk: Synchronization** → Sending messages synchronously might slow down the API. **Mitigation**: We will rely on Kafka's underlying asynchronous buffers and only wait for critical delivery confirmations if necessary.
