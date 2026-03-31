# Goal Description
This project migrates the hardcoded `KilnSimulatorScheduler` into a dynamic, database-driven entity model. Currently, the application manually simulates three kilns (KILN-01, KILN-02, KILN-03) with hardcoded state transitions and thresholds. 

The goal is to allow administrators to manage Kilns via the UI (creating, updating, toggling on/off), assign specific operating thresholds (Baseline, Warning, Critical temp limits), define Kiln Types (Electric, Gas, Wood-Fired, etc.), and configure probability limits for anomaly states (`warning_probability` and `critical_probability`). Simulation for a Kiln will only occur if the Kiln is active and has active user subscriptions.

# User Requirements
- A database model to persist `Kiln` attributes including Name, Type, Status, Thresholds, State Volatility Duration (`N`), and State Probabilities.
- A subscription model to track which UI clients are currently monitoring which kilns.
- Only simulate readings and push Kafka messages if a kiln is active `ON` AND has `subscribers > 0`.
- Only `ROLE_GATEWAY_ADMIN` users can manage Kilns (create, update, delete).
- Standard users can list active Kilns and subscribe to them for real-time telemetry.
- Real-time updates push via Server-Sent Events (SSE). The SSE stream will multiplex both telemetry events (`event: telemetry`) and configuration updates (`event: config_updated`) to gracefully handle runtime kiln rename/modification.
- A server-side caching registry (e.g., `KilnSimulationRegistry`) tracks active kilns and connections to avoid querying the DB during the high-frequency tick of the simulator loop.

# Impact
These changes will drastically improve the flexibility and accuracy of the simulator, establish a foundational multi-tenant capable entity structure, and significantly optimize the simulator loop by eliminating arbitrary calculation and db reads.

### New Capabilities
- Admin CRUD APIs for Kilns.
- Granular control over Kiln operational behaviors (Types, Temperatures, Noise levels, Volatility).
- Demand-driven Simulation (Subscribers must be > 0).

### Modified Capabilities
- Refactored `KilnSimulatorScheduler`.
- Updated React/Ionic UI caching logic for SSE to listen for `config_updated`.
