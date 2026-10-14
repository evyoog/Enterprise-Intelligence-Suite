# TC-SUB-029: The internal check needs the shared secret

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SUB-029 |
| Requirement ID (required) | [REQ-SUB-002](../../../docs/02-requirements/FRD/entitlements/requirement.md) (.5; BR-5) |
| Acceptance Criterion | [AC-6](../../../docs/02-requirements/FRD/entitlements/acceptance-criteria.md) |
| Decision | [C97](../../../docs/01-business/roadmap/open-decisions.md#c97) |
| Test Plan | [TESTPLAN-SUB-002](TESTPLAN-SUB-002.md) |
| Priority | P0 |
| Type | Security |
| Automated | Yes |

## Preconditions
The backend running with `vyoog.internal.sso-shared-secret` set.

## Steps
1. POST `/internal/entitlements/check` with no `X-Internal-Sso-Secret`.
2. Repeat with a wrong secret.
3. Repeat with the right secret.

## Expected Result
Steps 1 and 2: 403. Step 3: 200 with a normal answer (allowed=false for an unknown customer).

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/config/EntitlementsAuthorizationTest.java` — `theInternalCheckNeedsTheSharedSecret`

## Actual Result
The automated tests below passed on 2026-10-13 (`mvn -B -o test -Dtest=...`).

## Status
Passed (automated run 2026-10-13)

## Linked Defect (if failed)
None.
