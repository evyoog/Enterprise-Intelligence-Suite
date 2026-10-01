# TC-MKT-020: The empty cart shows Browse products

| Field | Value |
|---|---|
| Test Case ID (required) | TC-MKT-020 |
| Requirement ID (required) | [REQ-MKT-003](../../../docs/02-requirements/FRD/cart-checkout/requirement.md) (C59) |
| Acceptance Criterion | [AC-17](../../../docs/02-requirements/FRD/cart-checkout/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-MKT-003](TESTPLAN-MKT-003.md) |
| Priority | P2 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An empty cart.

## Steps
1. Open `/cart`.

## Expected Result
Icon, "Your cart is empty", "Find a product to get started." and a Browse products link to the catalog.

## Automated coverage
- `frontend/src/pages/CartPage.test.tsx` — "shows the empty state with Browse products"

## Actual Result
The automated tests passed on 2026-10-01.

## Status
Passed (automated run 2026-10-01)

## Linked Defect (if failed)
None.
