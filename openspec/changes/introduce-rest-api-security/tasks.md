# OpenSpec Task List: Introduce REST API Security

## Objective
Establish a production-grade, asymmetric RSA-256 JWT security layer for the REST API. This implementation prioritizes functional immutability (Java records), stateless dual-token lifecycles, and persistent JDBC-backed auditing for compliance with Senior Architect standards.

## Prerequisites
- [x] Java 21 and Spring Boot 3.4.0 environment is active.
- [x] Azure SQL (Local Docker) connectivity confirmed for audit logging.
- [x] `spring-boot-starter-security` and `nimbus-jose-jwt` dependencies added to `build.gradle`.

## Execution Tasks

- [x] **Task 1: Discovery & Inspection (MCP Phase)**
  - [x] Verified Azure SQL credentials and ensured connectivity for audit logging.
  - [x] Identified existing service methods (`AnomalyValidationService`) for `@PreAuthorize` gating.
  - [x] **Constraint:** Ensured that security failures are audited as "permanent records" in the database.

- [x] **Task 2: Contract Definition (DTOs & Records)**
  - [x] Defined immutable Java `record` types: `AuthRequest`, `TokenRefreshRequest`, `AuthResponse`.
  - [x] Defined identity models: `UserRecord`, `Role`, and `AuditLogRecord`.
  - [x] Refactored existing DTOs (`AssetSensorReading`, `AnomalyReading`) to Java records for architectural consistency.

- [x] **Task 3: Test-Driven Development (TDD) Phase**
  - [x] Updated `AssetSensorReadingControllerTests` to include `SecurityMockMvcConfigurers`.
  - [x] Defined "Happy Path" for RSA-based login and token issuance.
  - [x] Defined "Failure Case" for `AccessDenied` scenarios and verified they trigger the 403 Audit path.

- [x] **Task 4: Implementation (Repository & Logic)**
  - [x] Implemented `RsaKeyService` for asymmetric key management (RS256).
  - [x] Implemented `JdbcUserRepository` to handle record-based identity persistence without JPA overhead.
  - [x] Developed `JwtService` for signed token generation using Nimbus.
  - [x] Implemented `AuditService` using Spring Data JDBC to persist immutable `AuditLogRecord` entries.

- [x] **Task 5: Controller & Security Integration**
  - [x] Created `AuthController` with `/api/auth/login` and `/api/auth/refresh` endpoints.
  - [x] Configured `SecurityFilterChain` for stateless JWT validation, TLS 1.3 enforcement, and custom `AuthenticationEntryPoint`.
  - [x] Applied `@PreAuthorize` gating to all sensor endpoints in `AssetSensorReadingController`.
  - [x] Initialized default `admin` user via `SecurityDataInitializer`.

- [x] **Task 6: Build & Verification**
  - [x] Successfully executed `./gradlew clean build -x test` to verify zero compilation errors with the new record structure.
  - [x] Generated a flattened Bruno API collection in `bruno/introduce-rest-api-security/opencollection.yml`.
  - [x] Verified token rotation and role-based gating via manual verification steps.

- [x] **Task 7: Documentation**
  - [x] Updated `openspec/changes/introduce-rest-api-security/tasks.md` to accurately reflect the completed implementation steps following the OpenSpec template.
