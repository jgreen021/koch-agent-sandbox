# Design Document
This document details the architectural evolution transitioning from the hardcoded `KilnSimulatorScheduler` to an entity-driven simulation engine.

## Database Schema (JPA Entities)

### `Kiln` (Entity)
- `id` (UUID or Long, PK)
- `name` (String, unique constraints)
- `kilnType` (`Enum`: ELECTRIC, GAS, WOOD_FIRED, RAKU, CONTINUOUS, HOFFMANN, GLASS, HEAT_TREATING)
- `isActive` (Boolean)
- `baselineTemp` (Double)
- `warningTemp` (Double)
- `criticalTemp` (Double)
- `stateDurationSeconds` (Integer) - Determines how long an anomaly block lasts ("N").
- `warningProbability` (Double) - Percentage chance (0.0 to 1.0) of entering Warning state.
- `criticalProbability` (Double) - Percentage chance (0.0 to 1.0) of entering Critical state.
- *Note on Probabilities:* The `Normal` state probability is implicit and calculated dynamically as `1.0 - (warningProbability + criticalProbability)`. 
  - **Validation Constraint:** The `KilnService` and `KilnRequest` DTO must enforce that `warningProbability + criticalProbability <= 1.0`. Any request exceeding 100% will trigger a `400 Bad Request`.

### `KilnSubscription` (Entity / Concept)
- Tracks active sessions. However, since the SSE connection is stateless at the DB level during real-time views, we will map subscriptions purely to an in-memory `ConcurrentMap` tracked by the `KilnSimulationRegistry` to count live connections per kiln. Storing long-term "favorites" in the DB will be a future enhancement.

## Core Infrastructure

### 1. The KilnSimulationRegistry (`@Service`)
An in-memory tracking bean that maintains:
- `ConcurrentMap<UUID, KilnConfig> activeKilns`
- `ConcurrentMap<UUID, Integer> subscriberCounts`
When an Admin updates a kiln via the API, the DB saves it, and the Registry is explicitly updated (or via `ApplicationEvent`). The `/api/kilns/{id}/stream` endpoint (SSE) increments the subscriber count on connect and decrements on disconnect.

### 2. The Simulator Loop (`KilnSimulatorScheduler`)
The `@Scheduled` method no longer loops over 1..3. It iterates over `KilnSimulationRegistry.getActiveKilns()`.
For each kiln:
- If `!kiln.isActive()` or `subscribers == 0`, SKIP.
- If State Expired: Pick a new state utilizing `kiln.getWarningProbability()` and `kiln.getCriticalProbability()`. Re-calculate the expiration token using `kiln.getStateDurationSeconds()`.
- Produce the final fuzzed output utilizing the correct targeted thresholds.
- Publish `AssetSensorReading` to Kafka.

### 3. Server-Sent Events Multiplexing
The `SseEmitter` implementation will push dual event types down the pipe:
- `event: telemetry` -> `{"kilnId": 1, "temperature": 250.0...}`
- `event: config_updated` -> `{"kilnId": 1, "newName": "Gas Alpha"...}`
The UI updates the header reactively when config changes arrive.

## Security & RBAC
- `@PreAuthorize("hasRole('GATEWAY_ADMIN')")` protects `POST /api/kilns`, `PUT /api/kilns/{id}`, `DELETE /api/kilns/{id}`.
- `@PreAuthorize("hasAnyRole('USER', 'GATEWAY_ADMIN')")` protects `GET /api/kilns`, `GET /api/kilns/{id}/stream`.
