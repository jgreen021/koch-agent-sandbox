# API Specification: Password Reset Operations

## POST /api/auth/forgot-password
### Request Body
- `username`: String (required)

### Response
- `202 Accepted` (generic response to mask existence)

## POST /api/auth/reset-password
### Request Body
- `token`: String (required, 64-char hex)
- `newPassword`: String (required, min 8 chars recommended)

### Responses
- `200 OK`: Password updated.
- `400 Bad Request`: Invalid or already used token.
- `410 Gone`: Token expired.
