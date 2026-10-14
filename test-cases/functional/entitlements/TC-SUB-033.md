# TC-SUB-033: The internal check answers normal refusals with a reason, and refuses a malformed body

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SUB-033 |
| Requirement ID (required) | [REQ-SUB-002](../../../docs/02-requirements/FRD/entitlements/requirement.md) (.4; BR-10) |
| Acceptance Criterion | [AC-10](../../../docs/02-requirements/FRD/entitlements/acceptance-criteria.md) |
| Decision | [C97](../../../docs/01-business/roadmap/open-decisions.md#c97) |
| Test Plan | [TESTPLAN-SUB-002](TESTPLAN-SUB-002.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
Backend running; the shared secret known.

## Steps
1. Check with neither customerId nor organizationId.
2. Check with a productId that does not exist.
3. Check with no productId (right secret).

## Expected Result
Step 1: 200, allowed=false, BAD_REQUEST. Step 2: 200, UNKNOWN_PRODUCT. Step 3: HTTP 400.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/registration/service/EntitlementServiceTest.java` — `suspendedCancelledExpiredAndUnknownGrantNothing` (BAD_REQUEST, UNKNOWN_PRODUCT)
- `backend/src/test/java/com/vyoog/eisplatform/config/EntitlementsAuthorizationTest.java` — `theInternalCheckNeedsTheSharedSecret` (400 without productId)

## Actual Result
The automated tests below passed on 2026-10-13 (`mvn -B -o test -Dtest=...`).

## Status
Passed (automated run 2026-10-13)

## Linked Defect (if failed)
None.
