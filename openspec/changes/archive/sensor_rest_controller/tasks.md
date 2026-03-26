# OpenSpec Task List: Sensor REST API Refactor

## Objective
Refactor the Sensor REST API to support dynamic filtering, pagination, and range-based queries using a generic Specification pattern. Ensure full compatibility with PrimeNG lazy-loading requirements.

## Execution Tasks

- [x] **Task 1: Discovery & Verification (MCP Phase)**
  - Use the MSSQL MCP tool to verify the `AssetSensorReadings` table schema.
  - Confirm that `readingId` is the Primary Key and `timestamp` is of type `datetime2`.

- [x] **Task 2: Repository Extension**
  - Update `AssetSensorReadingRepository` to extend `JpaSpecificationExecutor<AssetSensorReadingEntity>`.

- [x] **Task 3: Immutable Record Creation**
  - Define `AssetSensorReading` as a Java `record`.
  - Use `LocalDateTime` for the `timestamp` field to match database precision.

- [x] **Task 4: Implement Generic Specification Builder**
  - Create a `SpecificationFactory` that generates JPA Specifications for any `Comparable` field.
  - Support `equalTo`, `greaterThanOrEqualTo`, and `lessThanOrEqualTo`.

- [x] **Task 5: Range Filtering Support**
  - Update the controller to support ranged queries for numeric and date fields:
    - `readingValue`: `minReadingValue`, `maxReadingValue`
    - `timestamp`: `startTime`, `endTime`
  - Ensure compatibility with ISO-8601 strings via `@DateTimeFormat`.

- [x] **Task 6: TDD Validation (MockMvc)**
  - Write and verify tests for:
    - Paginated reading retrieval.
    - Timestamp equality filtering.
    - Numeric range filtering (`readingValue`).
    - Date range filtering (`startTime`/`endTime`).

- [x] **Task 7: Bruno Collection Export**
  - Generate a `sensor-api.yaml` (OpenCollection format) for manual verification of pagination and range queries.