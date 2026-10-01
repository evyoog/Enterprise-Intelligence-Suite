# TC-MKT-021: The top bar shows the cart badge on every signed-in page

| Field | Value |
|---|---|
| Test Case ID (required) | TC-MKT-021 |
| Requirement ID (required) | [REQ-MKT-003](../../../docs/02-requirements/FRD/cart-checkout/requirement.md) (C59) |
| Acceptance Criterion | [AC-18](../../../docs/02-requirements/FRD/cart-checkout/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-MKT-003](TESTPLAN-MKT-003.md) |
| Priority | P2 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A signed-in user with 3 items.

## Steps
1. Open any signed-in page.
2. Click the cart icon.

## Expected Result
The icon shows "3" and is announced as "Cart, 3 items"; clicking it opens `/cart`. With an empty cart no count is shown.

## Automated coverage
- `frontend/src/components/cart/CartButton.test.tsx` — "shows the item count in its accessible name and opens the cart"

## Actual Result
The automated tests passed on 2026-10-01.

## Status
Passed (automated run 2026-10-01)

## Linked Defect (if failed)
None.
