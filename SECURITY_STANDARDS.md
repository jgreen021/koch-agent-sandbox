# Security Standards: Antigravity Edge Filter

## 1. Authentication (AuthN)
- **Standard:** OpenID Connect (OIDC) compliant JWT.
- **Provider:** Spring Security 6.x (Spring Boot 3.x stack).
- **Requirements:** - Use `NimbusJwtDecoder` for token validation.
    - Claims must include `sub`, `roles`, and `iat`.

## 2. Authorization (AuthZ)
- **RBAC:** Roles must be prefixed with `ROLE_` (e.g., `ROLE_GATEWAY_ADMIN`).
- **Granular Control:** Use `@EnableMethodSecurity` to enforce permissions at the functional level.

## 3. Data Protection
- **Password Hashing:** Use `BCryptPasswordEncoder` with a strength of 12.
- **Transport:** Force TLS 1.3. Any `/propose` for networking must include a redirect from HTTP to HTTPS.

## 4. Error Sanitization
- **Requirement:** Implement a `@RestControllerAdvice` to catch `AccessDeniedException`. 
- **Constraint:** Return a generic `403 Forbidden` JSON record to the client; log the specific trace internally to the Azure SQL Audit log.