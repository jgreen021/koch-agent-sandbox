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

## Architectural & Coding Standards
1. **Functional Programming Paradigm:** Immutability is king. Use Java `record` types for all Data Transfer Objects (DTOs), events, and domain models to guarantee immutability. Core domain methods must be pure functions, producing no side effects and returning new instances rather than mutating existing state.
2. **Clean Code Principles:** Methods must be small, strictly follow the Single Responsibility Principle (SOLID), and have highly descriptive naming conventions. Avoid deep nesting and mutable local variables.
3. **Data Contracts:** All JSON serialization and deserialization must be handled by Jackson `ObjectMapper`. Do not use manual String formatting for JSON payloads.
4. **Stateless Logic Preference:** Business logic should utilize in-memory structures when possible to reduce database I/O, reserving the database strictly for permanent audit logs and alerts.
5. **Resilience:** Always assume ingress data is "dirty." Implement robust sanitization at the edge boundaries before parsing.

## Testing Strategy (TDD)
* **Test-Driven Development:** The agent must adhere to a strict Test-Driven Development (TDD) workflow. When executing tasks, write the failing unit test first, followed by the minimal implementation code required to pass the test, and finally refactor for functional purity.
* **Test Quality:** Tests must be deterministic, run quickly, and test public behaviors rather than internal implementation details.

## Agent Instructions
* Read this file before executing any specific change tasks.
* If a task contradicts these global standards (e.g., requesting mutable state where a pure function is possible), flag the conflict to the user before writing the code.