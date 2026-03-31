# Specs: demand-driven-kiln-simulation

## Functional Requirements

### Requirement: Renaming Resiliency
Subscriptions and telemetry must be decoupled from the kiln's display name.
- **GIVEN** a kiln `id: 123-abc` named `KILN-ALPHA`
- **WHEN** the kiln is renamed to `ALPHA Gas 1`
- **THEN** the message stream on `/api/sensors/stream/123-abc` must remain active and uninterrupted.

### Requirement: Demand-Driven Simulation
The simulator must only generate Kafka messages for kilns with active listeners.
- **GIVEN** a kiln `id: 123-abc` has zero active subscribers in the `KILN_SUBSCRIPTION` table
- **WHEN** the `KilnSimulatorScheduler` runs its cycle
- **THEN** it should skip generating telemetry for that specific kiln.

### Requirement: Security Stability
Failed connections must not clog the audit logs with access denials.
- **GIVEN** a client disconnects from an SSE stream
- **WHEN** the system dispatches to the `/error` endpoint
- **THEN** the request must be permitted and return a valid (though potentially empty) response without authorization errors.

### Requirement: Configuration Multiplexing
Clients must be notified of out-of-band configuration changes.
- **GIVEN** an admin updates a kiln threshold or name
- **WHEN** the change is saved in the database
- **THEN** the `KilnSimulationRegistry` must trigger a `config_updated` event on all active SSE streams for that kiln.
