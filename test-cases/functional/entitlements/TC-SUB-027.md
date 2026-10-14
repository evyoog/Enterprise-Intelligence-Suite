# TC-SUB-027: Suspended, cancelled, expired and pending subscriptions grant nothing

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SUB-027 |
| Requirement ID (required) | [REQ-SUB-002](../../../docs/02-requirements/FRD/entitlements/requirement.md) (.6; BR-1) |
| Acceptance Criterion | [AC-4](../../../docs/02-requirements/FRD/entitlements/acceptance-criteria.md) |
| Decision | [C97](../../../docs/01-business/roadmap/open-decisions.md#c97) |
| Test Plan | [TESTPLAN-SUB-002](TESTPLAN-SUB-002.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
One customer with subscriptions in the statuses SUSPENDED, CANCELLED, EXPIRED and PENDING_SUBSCRIPTION to different products.

## Steps
1. Call the internal check for each product.
2. Read the customer's entitlements.

## Expected Result
Every check answers allowed=false with reason NO_SUBSCRIPTION; the entitlement list is empty.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/registration/service/EntitlementServiceTest.java` — `suspendedCancelledExpiredAndUnknownGrantNothing`

## Actual Result
The automated tests below passed on 2026-10-13 (`mvn -B -o test -Dtest=...`).

## Status
Passed (automated run 2026-10-13)

## Linked Defect (if failed)
None.
