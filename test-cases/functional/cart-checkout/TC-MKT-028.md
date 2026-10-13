# TC-MKT-028: Several products can be added in a row

| Field | Value |
|---|---|
| Test Case ID (required) | TC-MKT-028 |
| Requirement ID (required) | [REQ-MKT-003](../../../docs/02-requirements/FRD/cart-checkout/requirement.md) (C93, REQ-MKT-003.10) |
| Acceptance Criterion | [AC-26](../../../docs/02-requirements/FRD/cart-checkout/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-MKT-003](TESTPLAN-MKT-003.md) |
| Priority | P1 |
| Type | Functional / UI |
| Automated | Yes |

## Preconditions
Signed in (unless stated); a product with a free plan and two paid plans, a second product with a paid plan, and a product with only a free plan.

## Steps
1. Click **Add to cart** on the first product.
2. Without leaving the page, click it on the second.

## Expected Result
Both are added; the badge shows 2; the page is still the catalog.

## Automated coverage
- `frontend/src/components/cart/useAddToCart.test.tsx` — `adds in place, stays on the page…`

## Actual Result
The automated test passed on 2026-10-13.

## Status
Passed (automated run 2026-10-13)

## Linked Defect (if failed)
None.
