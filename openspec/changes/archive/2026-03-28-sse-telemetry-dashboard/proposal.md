# Proposal: Real-Time SSE Telemetry Dashboard

## Motivation
The current IoT Edge Gateway successfully filters, evaluates, and routes sensor data through Apache Kafka. However, the system currently lacks a dynamic, real-time method to push these insights and telemetry limits directly to a frontend user interface. Without real-time visibility, operators rely on polling or out-of-band alerts to respond to critical Kiln warnings and anomalies, reducing overall operational efficiency.

## Proposed Solution
We propose implementing a full-stack real-time telemetry dashboard.
- **Backend**: Implement Server-Sent Events (SSE) via `SseEmitter` in Spring Boot to consume events from Kafka. Additionally, implement a Kiln Simulator (`@Scheduled` task) to generate mock sensor readings so the UI has active data to display.
- **Frontend**: Scaffold a new user interface utilizing Ionic Framework, React, Tailwind CSS, Zustand (client state), and TanStack Query (server state). A custom `useMonitoring` hook will seamlessly bridge the backend SSE stream directly into the TanStack Query cache.

## Scope
1. Scaffold Ionic + React UI architecture in the `/ui` directory.
2. Build Spring Boot `SseEmitter` controller (`SseConnectionManager`) and Kafka listener.
3. Integrate the SSE stream onto a real-time "Kiln Dashboard" view.
4. Support filtering modes (e.g., continuous streaming vs. critical alerts only).

## Impact
- **Affected Systems**: `AssetSensorReadingController.java`, Kafka Consumer configuration, and the previously purged `/ui` directory.
- **Dependencies**: Spring Web (SseEmitter), React, TanStack Query, Zustand, Ionic Framework.
