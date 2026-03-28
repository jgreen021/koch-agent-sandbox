## ADDED Requirements

Context: This specification must satisfy all Global Standards and Security & Governance Standards defined in the root project.md.

### Requirement: Asymmetric RSA Authentication
The system MUST support asymmetric JWT Bearer authentication using RSA keys as defined in the "Senior Architect" standards.

#### Scenario: RSA Signature Validation
- **WHEN** A request is made with a JWT signed with the correct RSA Private Key.
- **THEN** The system MUST successfully validate the token using the corresponding RSA Public Key and allow access.

#### Scenario: Mismatched Signature Failure
- **WHEN** A request is made with a JWT that is malformed or signed with an incorrect key.
- **THEN** The system MUST return an `HTTP 401 Unauthorized` and log the failure to the Azure SQL `audit_logs` table.

### Requirement: Immutable Dual-Token Contracts (Java Records)
The security API MUST use Java `record` types for all external contracts, explicitly supporting a dual-token (Access + Refresh) flow.

#### Scenario: AuthResponse Record Structure
- **WHEN** A successful login occurs at `POST /api/auth/login`.
- **THEN** The system MUST return a `record AuthResponse` containing both an `accessToken` and a `refreshToken` field.

#### Scenario: Immutable Token Rotation
- **WHEN** A valid `refreshToken` is provided to the `POST /api/auth/refresh` endpoint using a `record TokenRefreshRequest`.
- **THEN** The system MUST return a new `record AuthResponse` with rotated tokens without requiring the user to provide credentials again.

### Requirement: Auditing Compliance
The system MUST log all security-related failures to the Azure SQL database for governance and auditing.

#### Scenario: Access Denied Auditing
- **WHEN** An authenticated user with insufficient roles attempts to access a protected service method gated by `@PreAuthorize`.
- **THEN** The system MUST return an `HTTP 403 Forbidden` and asynchronously create an entry in the `audit_logs` table using an immutable `audit_logs` record.

### Requirement: Transport Layer Enforcement (TLS 1.3)
The system MUST ensure all communication is encrypted and redirected where necessary.

#### Scenario: HTTP to HTTPS Redirect
- **WHEN** A request is made to the REST API via plain `HTTP`.
- **THEN** The system MUST automatically redirect to the `HTTPS` version of the endpoint.
