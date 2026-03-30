Perfect. Here's a `CODE_REVIEW_GUIDELINES.md` file that captures the review style from the excellent reference you shared:

```markdown
# Code Review Guidelines

## Philosophy

Code reviews should **affirm what's working well first**, then suggest enhancements as natural improvements—not failures. Reviews should understand the codebase deeply, respect intentional architectural decisions, and provide specific, actionable guidance tied to project standards.

## Review Structure

### 1. Opening Context
- System overview (1-2 sentences)
- Current state acknowledgment
- Alignment with project.md and SECURITY_STANDARDS.md

### 2. Strengths Section (Organized by Layer/Component)
Lead with what's exceptional. Use **5-star emoji ratings** (⭐⭐⭐⭐⭐) for standout work.

**Format:**
```
### ✅ **[Component Name]** — ⭐⭐⭐⭐⭐

**Strengths:**
- Specific pattern or implementation
- Why it's excellent
- Code example if it clarifies

**Business/Architectural Benefit:**
- How this decision serves the project goals
```

**Examples of strong opening sections:**
- Architecture & Organization
- Security Implementation
- Real-Time Architecture
- State Management

### 3. Deep-Dive Code Examples
Show actual code from the repo with line numbers. Explain **why** it's good:

**Good example:**
```java
// SecurityConfig.java — exemplary RBAC pattern
@EnableMethodSecurity  // Enables @PreAuthorize on service methods
public class SecurityConfig {
    // ...
}
```
**Why:** Enforces authorization at both controller AND method level (defense in depth per SECURITY_STANDARDS.md)

### 4. Observations for Enhancement
Only after strengths. Format as **potential improvements**, not defects.

**Structure per enhancement:**
```
#### **[Number]. [Title]** ([Priority])

**Current State:**
[Show the code as-is]

**Context Understanding:**
[Explain why the current approach exists/is valid]

**Enhancement Opportunity:**
[Suggest a better alternative with code example]

**Why This Works:**
- Benefit 1
- Benefit 2

**Priority:** [LOW / MEDIUM / HIGH]
```

**Tone:** Respectful of trade-offs. Acknowledge if something is currently fine but could be better in the future.

### 5. Scoring Summary
Use a **structured table** with component-level scores:

| Layer | Component | Score | Notes |
|-------|-----------|-------|-------|
| **Backend** | Security | 10/10 | Perfect JWT + RBAC |
| **Backend** | Architecture | 9/10 | Clean packages; consider X in phase 2 |

**Scoring guidance:**
- 10/10 = Exemplary, production-ready, reference-quality
- 9/10 = Strong; minor enhancements for completeness
- 8/10 = Good; clear improvement areas
- 7/10 = Functional; needs attention
- <7/10 = Deficiencies requiring rework

### 6. Priority Enhancements (Ranked)
Organize by phase/sprint:

```
### Phase 2 (Next Sprint)
1. **[Feature/Fix]** — Benefit (Effort, Impact)
2. ...

### Phase 3 (Backlog)
...
```

### 7. Final Assessment
**One paragraph** that ties everything together:
- Overall grade (A/B/C/D)
- Key strengths highlighted
- Readiness for production/next phase

---

## Test Coverage Assessment

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


## Key Principles

### 1. Understand Before Critiquing
- Read `project.md` to understand architecture standards
- Read `SECURITY_STANDARDS.md` for security expectations
- Study the existing code patterns before suggesting changes
- **Never** assume a pattern is wrong without understanding why it exists

### 2. Affirmative-First Approach
- Start with what's working well (at least 60% of the review)
- Scores should be 7+/10 for production code (8-10 is normal)
- Enhancements are "nice-to-haves," not "must-fixes" unless truly critical
- Use language: "Consider...", "Enhancement opportunity...", "Could be improved..." (not "should be")

### 3. Be Specific
- Always include code line numbers and file paths
- Show actual before/after when suggesting changes
- Explain **why** a pattern is better, not just **that** it is
- Reference project standards: "per project.md line X"

### 4. Respect Architectural Decisions
- If something is intentional, acknowledge it
- Example: "You chose in-memory state (project.md line 24) which is correct for high-throughput. In production with X scale, consider..."
- Don't recommend downgrades or fundamental changes without strong justification

### 5. Prioritize Practically
- Mark enhancements as LOW/MEDIUM/HIGH
- Group by phase (current sprint, next sprint, backlog)
- Justify effort vs. impact
- Don't overwhelm with 20 issues; focus on 5-7 key ones

### 6. Security & Standards
- Always verify against SECURITY_STANDARDS.md
- Call out exemplary security patterns (e.g., RSA JWT, RBAC)
- Flag only genuine risks, not theoretical ones
- Cross-reference project.md architectural constraints

---

## Section Templates

### Strengths Section Template
```
### ✅ **[Component Name]** — ⭐⭐⭐⭐⭐

**Strengths:**
1. **[Pattern 1]** — [Why it's excellent]
2. **[Pattern 2]** — [Why it's excellent]

```java
// Code example
key implementation here
```

**Alignment with Standards:**
- ✅ Adheres to [project.md constraint]
- ✅ Implements [SECURITY_STANDARDS.md requirement]
```

### Enhancement Template
```
#### **[Number]. [Title]** ([Priority])

**Current State:**
```java
// Current implementation
```

**Context Understanding:**
[Why this is currently valid/intentional]

**Enhancement Opportunity:**
```java
// Proposed improvement
```

**Why This Works:**
- [Benefit]
- [Benefit]

**Priority:** [LOW/MEDIUM/HIGH]
```

---

## What NOT to Do

❌ **Don't:**
- Recommend downgrades or architectural reversions without strong justification
- Flag things as "issues" if they're intentional trade-offs
- Use absolute language ("must", "should") for non-critical items
- Suggest generic best practices without understanding project context
- Create false urgency on low-impact items
- Review without reading project.md and SECURITY_STANDARDS.md first

✅ **Do:**
- Verify assumptions against project documentation
- Lead with strengths and affirmation
- Use conditional language for enhancements ("could", "consider")
- Explain **why** changes matter
- Group changes by phase and priority
- Respect that working, intentional code is not broken

---

## Example Opening

**Good:**
> This is an enterprise-grade industrial IoT anomaly detection system adhering to strict OpenSpec standards (Java 21 records, immutable state, functional programming, stateless security). The application demonstrates exceptional real-time architecture, production-ready security, and thoughtful operational design.

**Bad:**
> This Spring Boot application has several issues that need to be addressed before production.

---

## When to Escalate

Flag to user (don't just recommend):
- Security vulnerabilities (not theoretical, actual CVEs)
- Architectural conflicts with project.md constraints
- Test coverage below 50% (per TDD standards in project.md)
- Hardcoded secrets in production code
- Missing required standards per SECURITY_STANDARDS.md

---

## Reference: Scoring Examples

**⭐⭐⭐⭐⭐ (10/10)** — Reference-quality implementation
- Exemplary pattern that should be replicated
- Exceeds project standards
- Example: "RSA-256 JWT with dual-token flow (exactly per SECURITY_STANDARDS.md)"

**⭐⭐⭐⭐ (9/10)** — Strong, production-ready
- Minor enhancements possible but not blocking
- Example: "Clean packages, solid SOLID principles; consider test coverage in next phase"

**⭐⭐⭐ (8/10)** — Good, functional
- Clear improvement areas but not critical
- Example: "Works well; could benefit from configuration externalization"

**⭐⭐ (7/10)** — Adequate, needs attention
- Multiple improvements recommended before release
- Example: "Functional but fragile; error handling needs work"

---

## Usage for Future Reviews

When requesting a code review in a new conversation, share:

1. **This guideline file** or reference it
2. **Recent push/commit info** to be reviewed
3. **Any specific areas** to focus on (optional)

Reviewer should:
1. Read this file first
2. Read `project.md` and `SECURITY_STANDARDS.md`
3. Follow the structure above
4. Produce a review in the same style as the reference example

---

## Reference Example

See `code-review-032826.md` in the repository for an exemplary review following these guidelines. Key characteristics:
- 5-star ratings used liberally for strengths
- Specific code examples with line numbers
- Enhancements framed as "opportunities," not defects
- Priority levels for each suggestion
- Scoring table for component-level breakdown
- Final grade with affirmative summary
```
