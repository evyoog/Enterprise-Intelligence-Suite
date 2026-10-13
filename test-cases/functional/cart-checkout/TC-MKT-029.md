# TC-MKT-029: A refused add shows the cart message

| Field | Value |
|---|---|
| Test Case ID (required) | TC-MKT-029 |
| Requirement ID (required) | [REQ-MKT-003](../../../docs/02-requirements/FRD/cart-checkout/requirement.md) (C93, REQ-MKT-003.10) |
| Acceptance Criterion | [AC-27](../../../docs/02-requirements/FRD/cart-checkout/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-MKT-003](TESTPLAN-MKT-003.md) |
| Priority | P1 |
| Type | Functional / UI |
| Automated | Yes |

## Preconditions
Signed in (unless stated); a product with a free plan and two paid plans, a second product with a paid plan, and a product with only a free plan.

## Steps
1. Make the cart refuse the add (for example the customer already has an active subscription).
2. Click **Add to cart**.

## Expected Result
The cart's message is shown in the notice; there is no View cart link; nothing is added.

## Automated coverage
- `frontend/src/components/cart/useAddToCart.test.tsx` — `shows the cart message when the add is refused`

## Actual Result
The automated test passed on 2026-10-13.

## Status
Passed (automated run 2026-10-13)

## Linked Defect (if failed)
None.
