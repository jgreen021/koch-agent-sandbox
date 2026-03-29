# Koch Anomaly Tracker - UI Documentation

This document describes the architecture, state management, and real-time data flow of the React-based Telemetry Dashboard.

## 1. Technical Stack
*   **Framework**: [Ionic React](https://ionicframework.com/docs/react) (Cross-platform hybrid UI)
*   **Build Tool**: [Vite](https://vitejs.dev/)
*   **State Management**: [Zustand](https://github.com/pmndrs/zustand) (Simple, lightweight global state)
*   **Data Fetching/Caching**: [TanStack Query v5](https://tanstack.com/query/latest)
*   **Real-time Streaming**: Server-Sent Events (SSE)
*   **Styling**: Tailwind CSS + Ionic Modern Theming (Dark Mode support)

---

## 2. Core Architecture

### Routing (`App.tsx`)
The app uses `IonReactRouter` with protected navigation:
*   `/login`: Entry point for unauthenticated users.
*   `/dashboard`: The primary monitoring hub.
*   Automatic redirects ensure users are gated by the JWT presence in the global store.

### Global Store (`src/store/useAppStore.ts`)
We use Zustand for high-performance, boilerplate-free state:
*   `token`: JWT for API access.
*   `refreshToken`: Preserved for session continuity.
*   `selectedAsset`: Currently monitored machine (e.g., "KILN-01").

---

## 3. Real-time Telemetry Pipeline

The heart of the UI is the **`useMonitoring`** custom hook. It manages the complex lifecycle of the SSE firehose.

### Lifecycle of an SSE Connection
1.  **Handshake**: Upon selecting an asset, a new `EventSource` is opened to `/api/sensors/stream/{assetId}`.
2.  **Auth Injection**: Since standard `EventSource` doesn't support custom headers, we securely pass the token via a specialized `token` query parameter.
3.  **Surgical Updates**: When a message arrives, we DON'T trigger a full page re-render. Instead, we use `queryClient.setQueryData` to surgically update the TanStack cache for that specific asset.
4.  **Historical Log**: The hook automatically maintains a rolling window of the last 50 readings in a separate cache key (`telemetry-log`).

### Resiliency & Auto-Healing
*   **The Auth Probe**: If the SSE connection errors, the hook immediately hits `/api/auth/me`. If a `401 Unauthorized` is returned (e.g., server restart), it clears the token and forces a redirect to login.
*   **The Watchdog**: A client-side watchdog monitors incoming traffic. If no message is received for **35 seconds**, the connection is considered "zombie" and the hook forces a clean reconnect.

---

## 4. Key Components

### `Dashboard.tsx`
*   **Status Indicators**: Displays `Connecting`, `Connected`, or `Reconnecting` based on the hook state.
*   **Surgical Renderers**: Directly listens to the TanStack cache to ensure millisecond-latency updates for sensor dials.
*   **Audit Cards**: Renders the rolling history of telemetry events.

### `Login.tsx`
*   Features a premium, glassmorphic design.
*   Handles credential submission and initializes the global store.

---

## 5. Deployment & Build
*   `npm run dev`: Starts the Vite dev server with proxying to the Spring Boot backend.
*   `npm run build`: Generates a highly optimized production bundle in the `dist/` directory.
*   **Proxying**: Configured in `vite.config.ts` to automatically route `/api` and `/stream` requests to `localhost:8080` during development.
