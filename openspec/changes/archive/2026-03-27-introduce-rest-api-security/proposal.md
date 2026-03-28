## Why

The current sensor reading API is exposed over HTTP with no security, which is a critical vulnerability. As specified in the `project.md` and `SECURITY_STANDARDS.md`, we must transition to a production-grade, "Senior Architect" level security architecture. This involves moving beyond "lean" HMAC configurations to a robust, asymmetric (RSA) signed stateless identity system that includes auditing, role-based method security, and immutable data contracts.

## What Changes

We will implement a high-compliance security layer following these core pillars:
1. **Asymmetric Identity (RSA)**: Implement JWT signing using RSA keys (RS256) instead of shared secrets, ensuring higher security for distributed environments.
2. **Immutable Data Contracts (Java Records)**: All security-related DTOs (login requests, token responses, audit events) will be implemented as Java `record` types to ensure functional purity.
3. **Stateless Dual-Token Lifecycle**: A robust Access + Refresh token flow will be implemented to handle short-lived credentials without sacrificing performance.
4. **Azure SQL Audit Logging**: Every authentication failure and "Access Denied" event will be persisted to an Azure SQL audit table for security governance.
5. **Method-Level Security**: Move beyond basic controller protection to granular `@PreAuthorize` checks at the service layer.
6. **Transport Layer Security**: Force TLS 1.3 and implement automatic HTTP-to-HTTPS redirection.

## Capabilities

### New Capabilities
- `rest-api-security`: A Senior Architect-level security implementation featuring RSA-signed JWTs, RBAC, and immutable audit logging.

### Modified Capabilities
- `sensor-rest-api`: Existing endpoints will be strictly gated by role-based method security and RSA-validated tokens.

## Impact

- **Infrastructure**: Requires RSA KeyPair (Private for signing, Public for validation). 
- **Persistence**: New `AuditRecord` entity (Java Record) and corresponding `audit_logs` table in Azure SQL.
- **Dependencies**: `spring-boot-starter-security`, `spring-boot-starter-oauth2-resource-server`, and `nimbus-jose-jwt`.
- **Breaking Changes**: All API clients must now perform RSA-based authentication and handle token refresh flows.
- **Protocol**: Forces all traffic over HTTPS (TLS 1.3).
