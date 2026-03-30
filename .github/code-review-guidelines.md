# Code Review Guidelines

## Philosophy

Code reviews should **affirm what's working well first**, highlighting production-grade patterns with star ratings (⭐⭐⭐⭐⭐). Suggest enhancements as natural evolutions rather than defects. Reviews must reflect a deep understanding of the specific implementation (including code snippets) and justify all assessments against `project.md` and `SECURITY_STANDARDS.md`.

## Review Structure

### 1. Opening Header
**Format:** `# 📊 Code Review: [Project Name] — Update [YYYY-MM-DD]`

### 2. 🌟 BACKEND REVIEW (Spring Boot)
Organize by major functional blocks or recent commits.

**Per Component Format:**
```markdown
### ✅ **[Component Name]** — ⭐⭐⭐⭐⭐
**File:** `[Filename].java`

[Brief narrative of why this is exemplary]

**What's Excellent:**
[Code snippet reflecting the best part of the implementation]

**Why This is Production-Grade:**
1. [Point 1 - e.g. Cross-Cutting Concern logic]
2. [Point 2 - e.g. Idempotent Guards]
3. [Point 3 - e.g. Guaranteed Cleanup]

**Architectural Benefit:**
- ✅ [Benefit 1]
- ✅ [Benefit 2]
```

### 3. ✅ **Test Coverage Assessment**
Detailed breakdown of new or modified test suites.

**Format:**
- **Files Added:** List of files
- **[File] Strengths:** Code examples + specific coverage highlights (e.g. Mockito usage, TDD adherence)
- **[Context] Coverage:** Checklist of specific scenarios (e.g. Unauthorized access → 401)
- **Quality Score:** `X/10`

### 4. 🌟 FRONTEND REVIEW (React + Ionic)
Focus on UX Excellence, Type Safety, and Real-Time Architecture.

**Per Component Format:**
```markdown
### ✅ **[Feature Name]** — ⭐⭐⭐⭐⭐
**File:** `[Filename].tsx`

**[Category: e.g. Security Implementation]:**
[Analysis + Code Snippets]

**[Category: e.g. Real-Time Validation UX]:**
[Analysis of logic and visual feedback]

**Accessibility & Navigation:**
- ✅ [Feature 1]
- ✅ [Feature 2]
```

### 5. ⚠️ **Observations for Enhancement**
Address technical debt or polish opportunities.

**Format:**
```markdown
#### **[Enhancement Title]** ([Priority: LOW/MEDIUM/HIGH])

**Current State:**
[Code Snippet]

**Current Behavior:** (The "Why it exists")
[Explain the current valid implementation]

**Enhancement Opportunity:**
[Proposed better approach with code]

**Why This Matters:**
- [Benefit 1]
- [Benefit 2]
```

### 6. 📊 **Commit Quality Scorecard**
A summary table for quick assessment.

| Aspect | Score | Assessment |
|--------|-------|------------|
| Backend Security | X/10 | [Brief note] |
| Performance | X/10 | [Brief note] |
| Frontend UX | X/10 | [Brief note] |
| Type Safety | X/10 | [Brief note] |

### 7. 🎯 **Implementation Highlights**
Numbered list of the top 3-5 "wins" of the commit.

### 8. 🏆 **Final Assessment**
**Format:**
- **Grade:** [A+ / A / B / etc.]
- **Summary Narrative:** One paragraph tying the review together.
- **Recommended Next Phase:** Numbered list of actionable next steps.

---

## Technical Checklists

### Backend Standards
- ✅ Spring AOP for cross-cutting security concerns
- ✅ Guava `LoadingCache` for bounded, memory-safe caching
- ✅ Response sanitization (Generic 403s via `GlobalExceptionHandler`)
- ✅ Audit logging for all security events

### Frontend Standards
- ✅ `unknown` typing over `any` in catch blocks
- ✅ Real-time form validation with visual feedback (Green/Red)
- ✅ `aria-live` and `aria-label` for accessibility
- ✅ Watchdog patterns for SSE streams

---

## When to Escalate
- Security vulnerabilities (CVEs or hardcoded secrets)
- Violations of `SECURITY_STANDARDS.md` (e.g., plain-text passwords)
- Missing required audit trails for sensitive endpoints
- Substantial architectural deviation from `project.md`

---

## Reference Example
For the full aesthetic and tone, always refer to `code-review-a` in the root directory.

## Additional Reference Concerning Tests

### Where Tests Live

**Backend Tests:**
- `src/test/java/com/koch/anomaly/` — Unit and integration tests for anomaly detection
  - `AnomalyValidationServiceTest.java` — Core anomaly detection algorithm
  - `AssetSensorReadingControllerTest.java` — REST endpoint authorization and data flow
  - `AssetSensorKafkaListenerTest.java` — Kafka consumer integration
  - `KilnSensorProducerTest.java` — Event publishing
  - `KilnSimulatorSchedulerTest.java` — Scheduled task execution
  - `SseConnectionManagerTest.java` — Server-sent events streaming

- `src/test/java/com/koch/security/` — Authentication and authorization tests
  - `AuthControllerIntegrationTest.java` — Login/refresh/logout flows with MockMvc
  - `AuthServiceResetTest.java` — Password reset with token validation
  - `SecurityIntegrationTest.java` — Role-based access control enforcement
  - `HashTest.java` — Password hashing verification

**Frontend Tests:**
- `ui/cypress/e2e/` — End-to-end tests (Cypress)
  - Authentication flows, navigation, form submission
- `ui/src/*.test.tsx` — Unit tests (Vitest)
  - Component rendering, hook behavior

### What to Verify When Reviewing Tests

When assessing test coverage, check for:

1. **TDD Adherence** (per project.md)
   - ✅ Tests exist for public behaviors
   - ✅ Tests verify outcomes, not implementation details
   - ✅ Test names clearly describe what they test
   - Example: `testAnomalyDetectedWhenReadingExceedsThreshold()` (good) vs `testIsAnomaly()` (vague)

2. **Security Tests**
   - ✅ Invalid credentials rejected
   - ✅ Expired tokens handled
   - ✅ Role-based access denials logged to audit trail
   - ✅ Password reset tokens expire correctly
   - ✅ Rate limiting enforced on forgot-password

3. **Anomaly Detection Tests**
   - ✅ Edge cases: first 9 readings, exactly 10 readings, 11+ readings
   - ✅ Boundary conditions: readings at threshold, just above/below
   - ✅ Rolling window maintains correct state
   - ✅ Kafka publishing on CRITICAL anomalies

4. **Integration Tests**
   - ✅ Full auth flow: login → JWT generation → token refresh
   - ✅ Controller → Service → Repository chain
   - ✅ Kafka listener processes messages end-to-end

5. **Frontend Tests**
   - ✅ SSE connection state transitions
   - ✅ React Query cache updates on message arrival
   - ✅ Protected routes redirect unauthorized users
   - ✅ Token expiry detected and logout triggered

### Test Quality Indicators

**Strong signs:**
- Tests use realistic data (actual API responses, not mocks)
- Integration tests use MockMvc or @WebMvcTest
- Security tests verify audit logs were created
- Tests are deterministic (no race conditions, timing issues)
- Test names match the format: `test[What][When][Then]`

**Red flags:**
- Tests that only verify mocks were called (don't test actual behavior)
- Hardcoded timestamps or UUIDs
- Tests that require specific execution order
- Missing error scenario tests
- No integration tests, only unit tests

### Scoring Test Coverage

When evaluating test quality in reviews:

- **10/10** — Comprehensive coverage (80%+), integration tests, security scenarios, edge cases documented
- **9/10** — Strong coverage, all critical paths tested, minor scenarios missing
- **8/10** — Good coverage, core functionality tested, integration gaps
- **7/10** — Adequate coverage, critical paths covered, lacks edge cases
- **<7/10** — Insufficient coverage, gaps in security or integration tests

### Example Review Language

**Good:**
> Test coverage is strong (10 backend test files + Cypress E2E). AuthServiceResetTest thoroughly validates token expiry and invalid reset attempts. AnomalyValidationServiceTest covers rolling window edge cases. Consider expanding integration tests for SSE connection drops and mobile reconnection scenarios.

**Bad:**
> Tests are missing. Add more tests.

---

## When to Flag Test Deficiencies

**Escalate to user (don't just recommend) if:**
- No tests visible for security-critical paths (auth, authorization)
- Test coverage <50% for new code
- No integration tests, only mocks
- Security tests don't verify audit logging
- E2E tests missing for happy path flows

**Recommend for next phase if:**
- Specific edge cases uncovered (e.g., SSE reconnection on slow network)
- Load testing not present
- No chaos engineering tests (what if Kafka fails?)
- Frontend error boundary not tested
