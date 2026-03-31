# OpenSpec Task List: demand-driven-kiln-simulation

## Objective
Implement a demand-driven kiln simulation and renaming-resilient telemetry pipeline by decoupling subscriptions from kiln names and using a persistent subscription model.

## Prerequisites
- The local MCP servers are active and connected.
- Access to the Oracle database for schema updates.

## Execution Tasks

- [ ] **Task 1: Discovery & Inspection (MCP Phase)**
  - Use `mcp_sqlcl` to verify `APP_USERS` and `KILN` tables.
  - Check for existing foreign key constraints that might impact the new `KILN_SUBSCRIPTION` table.
  - **Constraint:** Adhere to "Assume data is dirty" standard from project.md.

- [ ] **Task 2: Contract Definition (DTOs & Records)**
  - Define `KilnSubscriptionRequest` and `KilnSubscriptionResponse` as Java records.
  - Update `AssetSensorReading` to ensure the identifier is clearly marked as `kilnId` (UUID).

- [ ] **Task 3: Test-Driven Development (TDD) Phase**
  - Create `KilnSubscriptionServiceTest.java`.
  - Create `KilnSimulationRegistryTest.java` to verify subscriber count logic.
  - Define the "Happy Path" (successful subscription) and "Failure Case" (subscribing to non-existent kiln).

- [ ] **Task 4: Implementation (Persistence & Logic)**
  - Create `KILN_SUBSCRIPTION` table via SQL script.
  - Implement `KilnSubscription` entity and `KilnSubscriptionRepository`.
  - Refactor `KilnSimulationRegistry` to sync with the subscription table.
  - Update `KilnSimulatorScheduler` to check subscriber counts before generating telemetry.

- [ ] **Task 5: Controller & Stream Integration**
  - Update `AssetSensorReadingController` to expose `/api/sensors/stream/{kilnId}` (UUID-based).
  - Implement `/api/kilns/{id}/subscribe` and `/api/kilns/{id}/unsubscribe` endpoints.
  - **Security:** Update `SecurityConfig.java` to permit `/error` and ensure RBAC for subscription management.

- [ ] **Task 6: Build & Verification**
  - Run `./gradlew build` and `./gradlew test`.
  - Create a Bruno collection `demand-driven-simulation.yml` to verify SSE streams under the new identity model.
  - Verify that renaming a kiln in the DB no longer breaks active streams.

- [ ] **Task 7: Documentation**
  - Update `openspec/changes/demand-driven-kiln-simulation/tasks.md` with implementation details and verification results.
  - Update architectural diagrams in `docs/specs/architecture/kiln_management.md` if necessary.
