# Tasks: SSE Real-Time Telemetry Dashboard

## Phase 1: Discovery
- `[x]` Run MCP tool or queries to verify existing Kafka configuration (`spring.kafka.consumer.group-id`).
- `[x]` Audit existing `AssetSensorReadingController` to understand how the system extracts identity for security contexts.

## Phase 2: Contract
- `[x]` Define a new Bruno OpenCollection YAML file (`bruno/sse-telemetry-dashboard`) representing the expected JSON event format and connection status for `/api/sensors/stream/{kilnId}`.

## Phase 3: TDD
- `[x]` Write failing unit tests for `SseConnectionManager` verifying concurrent subscription registration and cleanup logic.
- `[x]` Write failing unit tests checking the `AssetSensorKafkaListener` successfully delegates incoming payload fragments to the SseConnectionManager.
- `[x]` Write failing unit test verifying `KilnSimulatorScheduler` correctly publishes mock telemetry based on its cron schedule.
- `[x]` Write failing MockMvc test representing a client requesting `text/event-stream` media types with authentication.

## Phase 4: Backend Implementation
- `[x]` Implement `SseConnectionManager` using Java `ConcurrentHashMap`, capturing connected `SseEmitter` instances.
- `[x]` Add a new GET endpoint to `AssetSensorReadingController` returning `SseEmitter`.
- `[x]` Add ping/heartbeat capability to keep open streams active.
- `[x]` Wire the `AssetSensorKafkaListener` directly into the `SseConnectionManager` to push real-time broadcasts.
- `[x]` Implement a Spring `@Scheduled` task `KilnSimulatorScheduler` to fire periodic mock telemetry payloads to Kafka.

## Phase 5: Frontend Implementation
- `[x]` Scaffold a fresh Ionic project within the `/ui` directory.
- `[x]` Add Tailwind CSS, Zustand, TanStack Query dependencies to `package.json`.
- `[x]` Build out `useMonitoring` Custom Hook mapping `EventSource` events. Ensure hook exports connection states: `Empty | Loading | Connected | Reconnecting`.
- `[x]` Wire up a generic Dashboard View observing the TanStack store and connection states.

## Phase 6: Refinement
- `[x]` Review and address the "Recommendations for Further Improvement" section in `code-review-032726.md`.

