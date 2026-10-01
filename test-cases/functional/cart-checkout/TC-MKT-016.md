# TC-MKT-016: Change plan updates the totals at once and rolls back on a refusal

| Field | Value |
|---|---|
| Test Case ID (required) | TC-MKT-016 |
| Requirement ID (required) | [REQ-MKT-003](../../../docs/02-requirements/FRD/cart-checkout/requirement.md) (C59) |
| Acceptance Criterion | [AC-9](../../../docs/02-requirements/FRD/cart-checkout/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-MKT-003](TESTPLAN-MKT-003.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A cart item whose product has two paid plans.

## Steps
1. Choose the other plan in Change plan.
2. Make the server refuse the change.

## Expected Result
The line and totals change immediately; on a refusal the previous plan and totals come back and the error is shown. A plan of another product is refused (400).

## Automated coverage
- `frontend/src/pages/CartPage.test.tsx` — "changes the plan immediately and rolls back when the server refuses"
- `backend/src/test/java/com/vyoog/eisplatform/modules/cart/service/CartServiceTest.java` — `freePlansAndPlansOfAnotherProductAreRefused`

## Actual Result
The automated tests passed on 2026-10-01.

## Status
Passed (automated run 2026-10-01)

## Linked Defect (if failed)
None.
