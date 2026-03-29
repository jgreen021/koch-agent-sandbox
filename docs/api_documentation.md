# Koch Anomaly Tracker API Documentation

This document describes the REST and SSE API endpoints for the Anomaly Tracker system. 

## Base URL
`http://localhost:8080`

## Authentication
Most endpoints require a **Bearer JWT Token**. 
- To obtain a token, use the `/api/auth/login` endpoint.
- For SSE streams, the token can also be passed via the `token` query parameter.

---

## 1. Security & Identity (`/api/auth`)

### [POST] /login
Authenticates a user and returns Access/Refresh tokens.
- **Request Body**: `AuthRequest` (username, password)
- **Response**: `AuthResponse` (accessToken, refreshToken, expiresIn)

### [GET] /me
Returns the identity of the currently authenticated user.
- **Security**: Bearer Token
- **Response**: String (username)

### [POST] /refresh
Generates new Access/Refresh tokens using a valid refresh token.
- **Request Body**: `TokenRefreshRequest` (refreshToken)
- **Response**: `AuthResponse`

### [POST] /password
Changes the password for the current user.
- **Security**: Bearer Token

### [POST] /forgot-password
Initiates a password reset flow (simulated).

### [POST] /reset-password
Completes a password reset using a token and a new password.

---

## 2. Telemetry & Monitoring (`/api/sensors`)

### [GET] /readings
Retrieves a paginated list of historical sensor data.
- **Security**: Bearer Token
- **Parameters**:
  - `assetId` (Optional)
  - `startTime` / `endTime` (Optional, ISO-8601)
  - `page` / `size` (Pagination)
- **Response**: `PaginatedReadings`

### [POST] /readings
Manually ingest a reading (primarily for legacy integration).
- **Security**: Bearer Token

### [GET] /stream/{assetId}
Real-time Server-Sent Events (SSE) stream for telemetry.
- **Security**: Bearer Token (Header or `token` query param)
- **Content-Type**: `text/event-stream`
- **Events**: Returns JSON message of `AssetSensorReading` as they happen.

---

## 3. Data Models

### AssetSensorReading
| Field | Type | Description |
|---|---|---|
| `readingId` | Integer | Unique identifier |
| `assetId` | String | e.g. "KILN-01" |
| `sensorType` | String | e.g. "TEMPERATURE" |
| `readingValue` | Double | The numeric reading |
| `uom` | String | Unit of measure (e.g. "C") |
| `timestamp` | DateTime | ISO-8601 |
| `status` | String | `NORMAL`, `WARNING`, `CRITICAL` |

---

## 4. Error Handling
All errors follow a standard RFC-compatible structure:
```json
{
  "timestamp": "2026-03-28T19:45:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid credentials",
  "path": "/api/auth/login"
}
```

> [!TIP]
> You can find the raw OpenAPI 3.0 specification in [openapi.yaml](./openapi.yaml).
