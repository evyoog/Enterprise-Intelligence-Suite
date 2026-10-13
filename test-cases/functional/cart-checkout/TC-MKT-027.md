# TC-MKT-027: Add to cart on a card adds the cheapest paid plan and stays on the page

| Field | Value |
|---|---|
| Test Case ID (required) | TC-MKT-027 |
| Requirement ID (required) | [REQ-MKT-003](../../../docs/02-requirements/FRD/cart-checkout/requirement.md) (C93, REQ-MKT-003.10) |
| Acceptance Criterion | [AC-25](../../../docs/02-requirements/FRD/cart-checkout/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-MKT-003](TESTPLAN-MKT-003.md) |
| Priority | P1 |
| Type | Functional / UI |
| Automated | Yes |

## Preconditions
Signed in (unless stated); a product with a free plan and two paid plans, a second product with a paid plan, and a product with only a free plan.

## Steps
1. Open the Products catalog.
2. Click **Add to cart** on the product with several plans.

## Expected Result
The cheapest paid plan is added (`POST /me/cart/items`); the page does not change; the notice "Added {product} — {plan} to your cart" with a **View cart** link appears; the badge shows the new count. The free-only product has no Add to cart button.

## Automated coverage
- `frontend/src/components/cart/useAddToCart.test.tsx` — `picks the cheapest paid plan…`, `adds in place, stays on the page…`

## Actual Result
The automated test passed on 2026-10-13.

## Status
Passed (automated run 2026-10-13)

## Linked Defect (if failed)
None.
