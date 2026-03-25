# Task Spec: [Feature Name]

## 1. Objective
[Briefly describe the business value and technical goal.]

## 2. Discovery & Inspection (MCP Phase)
- [ ] **Task 1: Schema/Context Verification**
  - Use MCP tools to inspect [Table Name/Existing Classes].
  - Verify data types, constraints, and existing relationships.
  - **Constraint:** Adhere to "Assume data is dirty" standard from project.md.

## 3. Contract Definition (DTOs & Records)
- [ ] **Task 2: Draft Immutable DTOs**
  - Define Java `record` types for [Request/Response].
  - Ensure Jackson annotations are used for JSON mapping.

## 4. Test-Driven Development (TDD) Phase
- [ ] **Task 3: Failing Unit Test**
  - Create `[ClassName]Test.java` using JUnit 5/MockMvc.
  - Define the "Happy Path" and "Failure Case" (e.g., 404 or 400).
  - **Constraint:** Test public behavior, not internals.

## 5. Implementation (The "Minimal Pass")
- [ ] **Task 4: Repository/Persistence Layer**
  - Implement/Update Repository with `JpaSpecificationExecutor`.
- [ ] **Task 5: Logic & Controller**
  - Implement pure functions for business logic.
  - Create REST endpoints returning `ResponseEntity<Page<DTO>>`.

## 6. Refactor & Verify
- [ ] **Task 6: Functional Purity Check**
  - Remove any mutable local variables or side effects.
  - Ensure all logic is expressed as pure functions.
  - Ensure method signatures are clear and concise.
  - Ensure method parameters are declared as final when appropriate.

## 7. Build & Integration Test
- [ ] **Task 7: Build & Integration Test**
  - Run `./gradlew build`.
  - Verify pagination/sorting compatibility with PrimeNG.

## 8. Documentation
- [ ] **Task 8: Documentation**
  - Update `openspec/changes/[feature-name]/tasks.md` with the implementation details.

## 9. Verification
- [ ] **Task 9: Verification**
  - Run `./gradlew test`.
  - Provide the Bruno/Postman collection JSON for verification.