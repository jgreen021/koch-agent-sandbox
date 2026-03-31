# OpenSpec Task List: dynamic-kiln-simulator

## Objective
Migrate the hardcoded `KilnSimulatorScheduler` into a dynamic, database-driven entity model to allow UI subscription, custom threshold properties, and active status toggling.

## Prerequisites
- The local MCP servers are active and connected.
- The `KILN` table schema has been identified or prepared.

## Execution Tasks

- [x] **Task 1: Discovery & Architecture (MCP Phase)
  - Use MCP tools to inspect existing DB tables or codebase integration points.
  - **Constraint:** Adhere to "Assume data is dirty" standard from project.md.

- [x] **Task 2: Contract Definition (DTOs & Records)**
  - Define Java `record` types for `KilnRequest` / `KilnResponse`.
  - Implement `KilnType` Enum.
  - Ensure Jackson annotations are used.

- [x] **Task 3: Test-Driven Development (TDD) Phase**
  - Create Unit Tests using JUnit 5/MockMvc.
  - Define the "Happy Path" and "Failure Cases" for the `KilnController` CRUD operations.
  - Enforce `400 Bad Request` testing for `warningProbability + criticalProbability > 1.0`.

- [x] **Task 4: Implementation (Domain & Integration)**
  - Update Oracle schema/flyway to create the `KILN` table.
  - Create the `Kiln` JPA Entity.
  - Build `KilnSimulationRegistry` component maintaining thread-safe `ConcurrentHashMap` for configurations and subscriber counts.
  - Refactor `KilnSimulatorScheduler` to iterate exclusively over the `KilnSimulationRegistry`.

- [x] **Task 5: Controller & UI Integration (SSE)**
  - Implement `KilnController` and `KilnService` for Admin CRUD operations protected by `ROLE_GATEWAY_ADMIN`.
  - Update the SSE API (`/api/kilns/{id}/stream`) to increment/decrement subscriber counters in the Registry upon SseEmitter connect/disconnect.
  - Ensure the SSE Stream Multiplexes telemetry and `config_updated` payloads.

- [x] **Task 6: Build & Verification**
  - Run `./gradlew build` and `./gradlew test`.
  - Generate supporting API files (Bruno collection) for manual verification.

- [x] **Task 7: Documentation & Handoff**
  - **Java Backend:** Generate Javadoc for the new `Kiln` entity and `KilnSimulationRegistry`. Update `kiln_management.md` architecture doc if necessary. Create Bruno API Collection to document and test the Kiln CRUD endpoints.
  - **UI/Frontend:** Update documentation if the `config_updated` SSE event requires a new UI component pattern.
  - **Internal Process:** Update `openspec/changes/dynamic-kiln-simulator/tasks.md` checkboxes upon completion.
