# TC-MKT-026: Another user's cart item returns 404

| Field | Value |
|---|---|
| Test Case ID (required) | TC-MKT-026 |
| Requirement ID (required) | [REQ-MKT-003](../../../docs/02-requirements/FRD/cart-checkout/requirement.md) (C59) |
| Acceptance Criterion | [AC-24](../../../docs/02-requirements/FRD/cart-checkout/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-MKT-003](TESTPLAN-MKT-003.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
Users A and B each have a cart.

## Steps
1. As B, PATCH and DELETE an item ID from A's cart.

## Expected Result
404 each time; A's cart is unchanged.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/cart/service/CartServiceTest.java` — `anotherUsersItemIsNotFound`

## Actual Result
The automated tests passed on 2026-10-01.

## Status
Passed (automated run 2026-10-01)

## Linked Defect (if failed)
None.
