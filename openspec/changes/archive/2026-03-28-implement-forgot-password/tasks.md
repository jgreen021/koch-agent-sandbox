# Task: Implement Stateless Forgot-Password Workflow

## Phase 1: Discovery & Contract
- [x] Implement DDL for `PASSWORD_RESET_TOKENS` (Oracle 23c)
- [x] Create `PasswordResetRequest` and `PasswordResetCompleteRequest` Java records (DTOs)
- [x] Define the `AuthResetController` endpoints in `AuthController`

## Phase 2: TDD (Unit Tests)
- [x] Create `AuthServiceResetTest` to verify token generation logic
- [x] Verify token expiration (30m limit)
- [x] Ensure user existence is masked in responses (202 Accepted)
- [x] Implement failing test for Rate Limiting (3 requests/hour)
- [x] Implement failing test for Password Strength (length, complexity)
- [x] Implement failing test for Token Invalidation (previous tokens cleared on success)

## Phase 3: Implementation
- [x] Implement `ResetTokenRepository` (JDBC)
- [x] Code the `forgotPassword` logic in `AuthService` (with rate limiting)
- [x] Code the `resetPassword` logic (update app_users hash & enforce strength)
- [x] Implement `ResetTokenRepository.invalidatePreviousTokens(Long userId)` 
- [x] Secure the endpoints in `SecurityFilterChain`

## Phase 4: Verification (Bruno)
- [x] Create `forgot_password` Bruno request
- [x] Create `reset_password` Bruno request
- [x] Perform E2E "fire test" from token request to successful login
