## ADDED Requirements

### Requirement: Manual Sensor Data Injection
The system SHALL expose a REST endpoint to manually inject `AssetSensorReading` events into the backend processing pipeline via Kafka.

#### Scenario: Successful reading submission
- **WHEN** a HTTP `POST` request is sent to `/api/sensors/readings` with a valid JSON payload matching the `AssetSensorReading` schema.
- **THEN** the system SHALL return a `201 Created` response and publish the reading to the `kiln-sensor-readings` topic.

### Requirement: Kafka Message Production
The system SHALL utilize a dedicated producer service to broadcast validated sensor readings to the internal Kafka cluster.

#### Scenario: Message published to kiln topic
- **WHEN** the `KilnSensorProducer` service receives a reading from the controller.
- **THEN** it SHALL use a `KafkaTemplate` to send the JSON representation of the reading to the `kiln-sensor-readings` topic.

### Requirement: Industrial Format Compatibility
The producer SHALL ensure that all outgoing messages are serialized in a format that the `KilnSensorConsumer` can parse.

#### Scenario: Serialization to JSON
- **WHEN** an `AssetSensorReading` is sent to Kafka.
- **THEN** the message payload SHALL be a valid JSON string produced by Jackson `ObjectMapper`.
