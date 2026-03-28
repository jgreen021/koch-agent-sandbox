# Design: Stateless Forgot-Password Workflow

## Architectural Background
The application currently uses a stateless JWT-based identity model with Oracle 23c as the persistence layer. To maintain statelessness at the protocol level (REST), but support session-based resets, we will use a "Soft State" token pattern.

## Data Design (Oracle 23c)
A new `PASSWORD_RESET_TOKENS` table will be introduced to track lifecycle:
- `ID`: Primary Key (Identity)
- `USER_ID`: Foreign Key to `APP_USERS`
- `TOKEN`: String (64-character SecureRandom hex)
- `EXPIRY`: Timestamp
- `USED`: Boolean (Oracle 23c supported or NUMBER(1))

## Process Flow
1.  **POST `/api/auth/forgot-password` (Public)**:
    - Receive `username`.
    - Check if user exists.
    - If yes, generate token and expire old ones for that user.
    - Return `202 Accepted` regardless of existence.
2.  **POST `/api/auth/reset-password` (Public)**:
    - Receive `token` and `newPassword`.
    - Verify token existence, matching user, and non-expiry.
    - Update the user's password using `BCryptPasswordEncoder(12)`.
    - Mark token as `USED`.

## Security Considerations
- **SecureRandom**: Tokens must be generated using `java.security.SecureRandom`.
- **Timing Attacks**: The `forgot-password` response time should be normalized to prevent user enumeration.
- **Audit Trails**: All reset phases must be recorded in the `AUDIT_LOGS` table with the user's ID or IP context.

## Risks / Trade-offs
- **Email Simulation**: Since we aren't implementing an SMTP server in this phase, the token will be provided in the server logs (for development/demo purposes).
- **Concurrency**: Multiple simultaneous reset requests will follow a "Newest Wins" strategy, invalidating older tokens.
