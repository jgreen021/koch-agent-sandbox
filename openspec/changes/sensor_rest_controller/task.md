1. Discovery & Verification (MCP Phase)
[ ] Task 1: Entity Inspection

Use the MSSQL MCP tool to verify the AssetSensorReadings table schema.

Confirm that readingId is the Primary Key and timestamp is of type datetime2 or equivalent.

[ ] Task 2: Repository Extension

Update AssetSensorReadingRepository to extend JpaSpecificationExecutor<AssetSensorReadingEntity>.

2. Contract & DTO Definition
[ ] Task 3: Immutable Record Creation

Define AssetSensorReading as a Java record.

Note: Ensure LocalDateTime is used for the timestamp field to match the DB precision.

3. Generic Filtering Logic (The "Anti-Refactor" Step)
[ ] Task 4: Implement Generic Specification Builder

Instead of hardcoded filters, create a SpecificationFactory that creates a Specification for any Comparable field (supports readingId, timestamp, readingValue).

Constraint: Use a pure function to compose CriteriaBuilder predicates.

4. Controller & PrimeNG Integration
[ ] Task 5.1: Range Filtering Support
Update the controller to support ranged queries for numeric and date fields:
- readingValue: minReadingValue, maxReadingValue
- timestamp: startTime, endTime

Constraint: Use the existing logic in SpecificationFactory.

5. Verification
[ ] Task 6: TDD Validation
Write MockMvc tests that verify:
- Range filtering (e.g., readingValue between X and Y).
- Timestamp filtering using ISO-8601 strings (already done).

[ ] Task 7: Bruno Collection Export

Generate a sensor-api.bruno collection file for manual verification.