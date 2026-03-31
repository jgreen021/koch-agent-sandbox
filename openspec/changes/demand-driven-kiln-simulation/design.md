# Design: demand-driven-kiln-simulation

## Context

The current `KilnSimulatorScheduler` maintains an in-memory cache (`KilnSimulationRegistry`) that is only loaded once at startup. If a kiln is renamed in the database, the simulator remains out of sync, leading to a mismatch between generated telemetry (Kafka) and client subscriptions (SSE). Clients currently subscribe via `assetName`, making the system fragile to renames.

## Goals / Non-Goals

### Goals
- **UUID-based Subscriptions:** Decouple telemetry from human-readable names.
- **Demand-Driven Generation:** Transition to a "only simulate if needed" model to conserve resources.
- **Persistent Subscription Management:** Store user-to-box relationships in a relational table.
- **Improved Logging Hygiene:** Gracefully handle disconnection-triggered error dispatches.

### Non-Goals
- **Replacing Kafka/SSE:** We are sticking with the current messaging infrastructure.
- **Real-time Configuration Sync:** We are allowing for a small delay (e.g., 30s) for the simulation cache to refresh from the database.

## Decisions

### 1. Subscription Persistence (`KILN_SUBSCRIPTION` table)
- **Rationale:** Storing subscriptions in the DB allows the system to survive restarts and provides a source of truth for the simulator.
- **Schema:**
  - `id`: UUID (Primary Key)
  - `kiln_id`: UUID (Foreign Key to KILN.ID)
  - `user_id`: UUID (Foreign Key to APP_USERS.ID)
  - `created_at`: TIMESTAMP

### 2. Kiln Simulation Registry Refactor
- **Decision:** The `KilnSimulationRegistry` will now maintain the counts of active subscribers (both in-memory for fast access and periodically synced from DB).
- **Mechanism:** A `@Scheduled` task in the registry will refresh the memory cache from the `KILN` and `KILN_SUBSCRIPTION` tables every 30 seconds.

### 3. Telemetry Stream Routing
- **Decision:** The endpoint `/api/sensors/stream/{kilnId}` will replace the name-based endpoint.
- **Kafka Key:** The Kafka message key will transition from `assetName` to `kilnId`.
- **SSE Event Multiplexing:** We will use the `event:` field in SSE to distinguish between `telemetry` and `config_updated` events.

### 4. Security Configuration
- **Decision:** Permit `/error` in `SecurityFilterChain`.
- **Rationale:** Most disconnections result in an internal error dispatch. Denying it causes noisy and irrelevant `AuthorizationDeniedException` warnings.

## Risks / Trade-offs

- **Cache Incoherency:** There is a short window (30s) where manual DB changes might not be reflected in the simulation.
- **Complexity:** Moving from a simple name-based string to UUIDs increases the complexity of API calls for manual testing (requires finding the UUID first).


Me:
```text
After describing the issues and sharing the original design with the agent, it did create another proposal which I guess I would say is a phase 2 because it addresses many issues and feels like an iterative approach or a good delta. I wish I would have broken them up into iterative phases.
```
It sounds like you’ve just had a classic "Aha!" moment with the OpenSpec workflow. Realizing that a massive, all-encompassing design is often the enemy of a clean implementation is a huge step toward mastering the tool. 

Don't kick yourself too hard for not breaking it up initially—LLMs are notoriously confident, and they’ll often make you believe they can handle a 20-point architectural overhaul in one go until the code actually starts hitting the disk.

Since you've now got that "Phase 2" proposal that acts as a delta, you are actually in a much stronger position. Here is how to make the most of this iterative shift:

## 1. Embrace the "Delta" Mindset
Since this new proposal is targeted at the missed items and the bugs found in the "Apply" phase, treat it as a **surgical strike**. 
* **Reference the "As-Is":** Make sure the agent acknowledges the current state of the code (the working-but-incomplete version).
* **Isolation:** If the Phase 2 proposal still feels a bit heavy, you can literally tell the agent: *"Let's take only the first three items from this Phase 2 proposal and apply those first."* ## 2. The "Verify Before Apply" Gate
Before you move from this new **Propose** mode into the next **Apply** phase, try this specific prompt:
> "Compare this Phase 2 proposal against the original `design.md`. List exactly which 'missed' requirements this will fulfill and which ones are still being deferred. I want to ensure nothing slips through the cracks again."

## 3. Modularizing your `design.md`
For the future (and even for this Phase 2), you can structure your design documents to be **Milestone-based**. Instead of one long list of features, use headers like:
* **Milestone 1: Core API & Data Model**
* **Milestone 2: PrimeNG Component Integration**
* **Milestone 3: Validation & Edge Cases**

By doing this, you can tell the agent to "Apply Milestone 1" only. It keeps the context window focused and prevents the "middle-child syndrome" where the LLM gets tired halfway through writing your TypeScript services.

## 4. Turning the "Bug Fix Session" into a Feature
Since you had a bug fix session, you now have the best possible data for the AI: **Negative Constraints**. 
> "In the last phase, we ran into [Specific Bug]. Ensure this new proposal includes a specific unit test or validation check to prevent that regression."

It’s actually a lucky break that you kept that original conversation copy. You can even feed the "Apply" failure back to the agent and ask, *"Why did you miss these 4 points in the last implementation?"* Sometimes the AI will straight-up tell you: *"The design was too ambiguous regarding the PrimeNG table implementation,"* which gives you the exact hint you need to beef up the current spec.

How are you feeling about the complexity of this "Phase 2" proposal? Does it feel like something that can be knocked out in one "Apply" session, or should we look at breaking it down even further?