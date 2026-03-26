## Context

The current state of the sensor API is transitionary and lacks security. This update enforces "Senior Architect" standards (asymmetric signing, immutable records, and auditing) using Spring Security 6.x and OAuth2 Resource Server protocols as defined in the root `project.md`.

## Goals / Non-Goals

**Goals:**
- **Asymmetric Security**: Secure all REST endpoints using JWTs signed with RSA-256 KeyPairs. 
- **Immutable Contracts**: Ensure all security DTOs and audit events use Java `record` types to follow functional programming principles.
- **Audit Integration (Persistence-Driven)**: Directly log every authentication and authorization failure to a structured Azure SQL audit table using **Spring Data JDBC** to maintain `AuditLogRecord` as an immutable Java record.
- **Method-Level Security**: Move beyond basic controller gating to `@PreAuthorize` annotations on core service methods.
- **Transport Security**: Enforce TLS 1.3 for all ingress traffic.

**Non-Goals:**
- Implementation of a full-scale OIDC/OAuth2 Authorization Server. (We will implement a compliant, lightweight JWT provider internal to the service).
- Session-based authentication or `HttpSession` usage.
- Use of mutable JPA Entities for core audit logging.

## Decisions

1. **Authentication Strategy (Nimbus JWT)**: 
   - Use `NimbusJwtDecoder` for token validation as specified in `SECURITY_STANDARDS.md`.
   - Implement an `RsaKeyService` to load Public/Private keys from environment variables or secure references (Azure Key Vault).
2. **Immutable Identity (Java Records)**:
   - `record AuthRequest(String username, String password)`
   - `record AuthResponse(String accessToken, String refreshToken, long expiresIn)`
   - `record AuditLogRecord(LocalDateTime timestamp, String event, String username, String path, String reason)`
3. **Stateless Security Stack**:
   - `spring-boot-starter-oauth2-resource-server`: Standardize token parsing and validation.
   - `BCryptPasswordEncoder(12)`: For hashing credentials, matching the requested strength.
4. **Resilient Audit Service (Jdbc-Backed)**:
   - Create an `AuditService` that catches `AuthenticationFailureEvent` and `AccessDeniedException`.
   - Use **Spring Data JDBC** or **JdbcTemplate** to persist `AuditLogRecord` record types directly to Azure SQL, circumventing the mutability requirement of JPA entities.
5. **RBAC and Method Security**:
   - Roles like `ROLE_GATEWAY_ADMIN` and `ROLE_OPERATOR` will be used to protect specific functional logic at the service layer using `@PreAuthorize`.
6. **Transport (HSTS/Redirects)**:
   - Configure a `SecurityFilterChain` that forces HTTPS redirects and specifies TLS 1.3 using a `Customizer<HttpSecurity>`.

## Risks / Trade-offs

- **Key Management Complexity**: Moving to RSA requires managing a Private Key securely (Env vs. Key Vault), which is more complex than a simple HMAC secret.
- **Performance Overhead**: RSA-signed validation has a slight performance cost compared to HMAC-HS256, but is a requirement for "Senior Architect" compliance.
- **Persistence Choice**: Opting for Spring Data JDBC increases development of manual schema mappings for the audit table but guarantees immutability of the domain records.
