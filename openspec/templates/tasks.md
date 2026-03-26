# OpenSpec Task List: [Feature Name]

## Objective
[Briefly describe the business value and technical goal.]

## Prerequisites
- [e.g. The local MCP server is active and connected.]

## Execution Tasks

- [ ] **Task 1: Discovery & Inspection (MCP Phase)**
  - Use MCP tools to inspect [Table Name/Existing Classes].
  - Verify data types, constraints, and existing relationships.
  - **Constraint:** Adhere to "Assume data is dirty" standard from project.md.

- [ ] **Task 2: Contract Definition (DTOs & Records)**
  - Define Java `record` types for [Request/Response].
  - Ensure Jackson annotations are used for JSON mapping.

- [ ] **Task 3: Test-Driven Development (TDD) Phase**
  - Create `[ClassName]Test.java` using JUnit 5/MockMvc.
  - Define the "Happy Path" and "Failure Case" (e.g., 404 or 400).

- [ ] **Task 4: Implementation (Repository & Logic)**
  - Implement/Update Repository with `JpaSpecificationExecutor`.
  - Implement Specifications in a generic factory.

- [ ] **Task 5: Controller & PrimeNG Integration**
  - Create REST endpoints returning `ResponseEntity<Page<DTO>>`.
  - Ensure pagination/sorting compatibility.

- [ ] **Task 6: Build & Verification**
  - Run `./gradlew build` and `./gradlew test`.
  - Generate supporting files, if applicable, for manual verification.

- [ ] **Task 7: Documentation**
  - Update `openspec/changes/[feature-name]/tasks.md` with the implementation details.