# OpenSpec Task List: Azure SQL Integration Testing & Alignment

## Objective
Verify the Azure SQL integration, bring the `AnomalyValidationService` under strict test coverage (TDD alignment), and perform a live End-to-End (E2E) verification using the MSSQL MCP server.

## Execution Tasks

- [x] **Task 1: Unit Test Alignment (`AnomalyValidationServiceTest.java`)**
  - Update the existing unit tests to mock the new `AssetSensorReadingRepository`.
  - Write a test ensuring `repository.save()` IS called exactly once when a `CRITICAL` or `WARNING` reading is evaluated.
  - Write a test ensuring `repository.save()` IS NEVER called when an `OK` or `INSUFFICIENT_DATA` reading is evaluated.
  - Ensure the tests still verify the Jackson `ObjectMapper` Kafka serialization from previous steps.

- [x] **Task 2: Build & Unit Test Verification**
  - Run `./gradlew clean test`.
  - Do not proceed until all tests pass. If the agent's previous code fails these tests, refactor the implementation code to pass.

- [x] **Task 3: Live End-to-End (E2E) Verification Preparation**
  - Ensure the local Kafka container is running (`docker compose up -d`).
  - Start the Spring Boot application locally (`./gradlew bootRun`).

- [x] **Task 4: The Live Fire Test**
  - Send 50 identical JSON payload messages into the `kiln-sensor-readings` Kafka topic (e.g., `130.0` or a value that guarantees a CRITICAL alarm) using the Kafka CLI producer.
  - Wait for the Spring Boot logs to output the 'ALARM: CRITICAL' and 'Saved to DB' confirmation.

- [x] **Task 5: MCP Database Verification**
  - Use the `@modelcontextprotocol/server-mssql` tool to query the `AssetSensorReadings` table in the Azure SQL Database.
  - Retrieve the top 5 most recent rows ordered by descending created date.
  - Confirm the row exists, the `asset_id` matches the test data, and the `status` is accurately reflected.