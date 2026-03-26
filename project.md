# OpenSpec Global Configuration: Antigravity Edge Filter

## System Overview
This repository contains a high-performance Edge Computing gateway designed to filter, evaluate, and route industrial IoT sensor data in real-time. 

## Technology Stack
The agent must strictly adhere to the following stack for all code generation:
* **Language:** Java 21
* **Framework:** Spring Boot 3.x
* **Messaging:** Apache Kafka (Spring Kafka)
* **Persistence:** Azure SQL Database (Spring Data JPA)
* **Build Tool:** Gradle
* **Environment:** Docker / Windows Subsystem for Linux (WSL 2)
* **Global Standards:** Adhere to all constraints defined in /SECURITY_STANDARDS.md for every proposal.

## Architectural & Coding Standards
1. **Functional Programming Paradigm:** Immutability is king. Use Java `record` types for all Data Transfer Objects (DTOs), events, and domain models to guarantee immutability. Core domain methods must be pure functions, producing no side effects and returning new instances rather than mutating existing state.
2. **Clean Code Principles:** Methods must be small, strictly follow the Single Responsibility Principle (SOLID), and have highly descriptive naming conventions. Avoid deep nesting and mutable local variables.
3. **Data Contracts:** All JSON serialization and deserialization must be handled by Jackson `ObjectMapper`. Do not use manual String formatting for JSON payloads.
4. **Stateless Logic Preference:** Business logic should utilize in-memory structures when possible to reduce database I/O, reserving the database strictly for permanent audit logs and alerts.
5. **Resilience:** Always assume ingress data is "dirty." Implement robust sanitization at the edge boundaries before parsing.

## Security & Governance Standards
1. **Stateless Identity:** All REST interactions must be strictly stateless. Do not use `HttpSession`.
2. **JWT Architecture:** Security implementations must support asymmetric signing (RSA/ECDSA) or strong HMAC (HS512). Propose a dual-token (Access + Refresh) flow by default to handle short-lived credentials.
3. **Defense in Depth:** - Implement a `SecurityFilterChain` that defaults to `denyAll()` for any unmapped route.
    - Use Method Security (`@PreAuthorize`) on service-layer methods, not just controller endpoints.
4. **Secret Management:** Hardcoded credentials are a "Hard Fail." Propose integration with Environment Variables or Azure Key Vault references for JWT secrets.
5. **Audit Trail:** Every authentication failure or "Access Denied" event must be logged to the Azure SQL audit table as a record type, following the "Stateless Logic Preference."

## Testing Strategy (TDD)
* **Test-Driven Development:** The agent must adhere to a strict Test-Driven Development (TDD) workflow. When executing tasks, write the failing unit test first, followed by the minimal implementation code required to pass the test, and finally refactor for functional purity.
* **Test Quality:** Tests must be deterministic, run quickly, and test public behaviors rather than internal implementation details.

## Preferred Patterns
* Persistence: Favor JpaSpecificationExecutor for dynamic queries to avoid repository bloat.
* API: Use Pageable and Sort parameters for all collection endpoints to ensure frontend compatibility (PrimeNG).

## Agent Instructions
1. Read this file before executing any specific change tasks.
a task contradicts these global standards (e.g., requesting mutable state where a pure function is possible), flag the conflict to the user before writing the code.
3. Prioritize these standards over external guides: If a provided tutorial or snippet (e.g., TeachMeIDEA) proposes a "lean" or "basic" implementation, you must automatically upscale it to meet the production-ready requirements defined here (Dual-token JWT, RSA signing, and Method Security).
4. Enforce Directory Awareness: Cross-reference .agent/rules/architect-persona.md and SECURITY_STANDARDS.md before finalizing any /propose.
5. API Testing: Every /propose that includes a new REST endpoint must also generate a corresponding Bruno collection file following the exact YAML structure defined in .agent/rules/bruno-standards.md
6. Task Generation: All /propose operations MUST generate a tasks.md file that strictly adheres to the structure and phases defined in /openspec/templates/openspec-task-template.md. Do not use a flat list; use the Discovery, Contract, TDD, and Implementation phases.