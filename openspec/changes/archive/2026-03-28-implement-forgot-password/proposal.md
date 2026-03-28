# Proposal: Stateless Forgot-Password Workflow

## Problem Statement
After migrating from MSSQL to Oracle 23c and implementing dual-token JWT security, we lack a self-service recovery mechanism for credentials. Currently, password resets require direct database intervention, which is not scalable or secure for production environments.

## Proposed Solution
Introduce a stateless "Forgot Password" request flow that leverages One-Time Tokens (OTT) stored in the Oracle database. The process will follow a two-step handshake:
1.  **Request**: User identifies themselves. The server generates a high-entropy secret and records it with an expiry.
2.  **Verification & Reset**: User provides the secret alongside a new password. The server validates the token's validity, age, and usage status before updating the `APP_USERS` hash.

## Key Goals
- **Maintain Security**: Do not reveal if a username exists via response codes or response times.
- **Stateless Handshake**: The user does not need to be logged in to initiate the request.
- **Auditability**: Log all reset requests and outcomes in the `AUDIT_LOGS` table.

## Impact
- **Database**: Adds a new `PASSWORD_RESET_TOKENS` relational table.
- **REST API**: Adds two new unauthenticated POST endpoints.
- **Security**: Strengthens production readiness by eliminating "admin magic" for resets.


## Protecting Against Common Attacks
- Ensure that all endpoints are not vulnerable to the following attacks:
1. SQL Injection (SQLi):
 - Use Parameterized Queries or an ORM (like Hibernate or Entity Framework). Never concatenate strings to build a query
2. Cross-Site Scripting (XSS): 
 - Ensure all user inputs are properly sanitized before rendering in HTML. Use libraries like OWASP Java Encoder
 - Output Encoding: Encode all dynamic data before rendering it in HTML
 - CSP: Implement a Content Security Policy header to restrict where scripts can be loaded from
3. Cross-Site Request Forgery (CSRF): 
 - Use Anti-CSRF tokens for the initial request form and ensure the final "Reset Password" action is a POST request that requires the secret token from the email
4. Rate Limiting: 
 - Implement rate limiting based on IP address and target email. For example, allow only 3 reset requests per hour per username
5. Token Management: 
 - Implement a mechanism to prevent the same token from being used more than once.
 - Once the password is changed, invalidate all other active sessions for that user
6. Password Policy: 
 - Enforce strong password policies (length, complexity) during the reset. Use a library like zxcvbn to check for common passwords
