# OpenSpec Task List: kafka-rest-controller

## Objective
Implement a high-performance Kafka Producer service to broadcast `AssetSensorReading` events to the internal messaging pipeline, triggered via the existing REST controller.

## Prerequisites
- The local Apache Kafka cluster is active on `localhost:9092`.
- `KilnSensorConsumer` is available to verify message receipt.

## Execution Tasks

- [x] **Task 1: Discovery & Inspection (MCP Phase)**
  - Use MCP tools to inspect `AssetSensorReadingController.java` and `AssetSensorReading.java`.
  - Verify existing Kafka topic configurations in `KilnSensorConsumer.java`.
  - **Constraint:** Adhere to "Assume data is dirty" standard from project.md.

- [x] **Task 2: Contract Definition (DTOs & Records)**
  - Review the existing Java `record` type `AssetSensorReading` for producer compatibility.
  - Ensure Jackson annotations are properly configured for JSON serialization.

- [x] **Task 3: Test-Driven Development (TDD) Phase**
  - Create `KilnSensorProducerTest.java` using JUnit 5.
  - Test the "Happy Path" (successful publish) and "Failure Case" (Kafka downtime).

- [x] **Task 4: Implementation (Repository & Logic)**
  - Implement `KilnSensorProducer.java` using `@Service` and `KafkaTemplate`.
  - Configure `spring.kafka.producer` in `src/main/resources/application.yml`.

- [x] **Task 5: Controller & PrimeNG Integration**
  - Update `AssetSensorReadingController` to include a `POST` endpoint for reading submission.
  - Ensure the endpoint returns `201 Created` upon successful delegation to the producer.

- [x] **Task 6: Build & Verification**
  - Run `./gradlew build` and `./gradlew test`.
  - Manually verify using `curl` to POST data and observe consumer logs.
  - Create a Bruno collection in the format like seen in `sensor-api.yaml`.

- [x] **Task 7: Documentation**
  - Update `openspec/changes/kafka-rest-controller/tasks.md` with the implementation details.
