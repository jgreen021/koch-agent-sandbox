# Proposal: demand-driven-kiln-simulation

## Goal

The current kiln simulation system is "blind"—it generates telemetry for all active kilns regardless of whether anyone is watching. Furthermore, it uses the kiln's `name` as the identifier for both Kafka messages and SSE streams. This creates two problems:
1.  **Renaming Incompatibility:** If a kiln is renamed in the database, existing subscriptions break, and the UI stops receiving data.
2.  **Resource Inefficiency:** The system wastes CPU and network resources generating data for kilns that have no active listeners.

This proposal aims to transition to a **Demand-Driven Simulation** model.

## Changes

- **Immutable ID Subscriptions:** Transition all SSE and Kafka telemetry to use the Kiln's internal `UUID` as the primary key/identifier instead of the `name`.
- **Persistent Subscription Model:** Introduce a `KILN_SUBSCRIPTION` entity to track user-kiln relationships.
- **Smart Simulation:** Modify the `KilnSimulatorScheduler` to only process kilns that have at least one active subscriber.
- **Improved Resiliency:** Allow `/error` dispatches in Spring Security to eliminate noisy "Access Denied" logs during connection drop-offs.

## Impact

- **Database:** New `KILN_SUBSCRIPTION` table; migration of existing data if necessary.
- **API:** Updated endpoints for `/api/sensors/stream/{kilnId}` and new `/api/kilns/{id}/subscribe` endpoint.
- **Security:** Minor update to `SecurityConfig` to permit `/error`.
- **Infrastructure:** Telemetry flow on Kafka now uses UUID keys.
- **User Experience:** Seamless renaming of assets; more efficient backend scaling.
