# TC-MKT-011: Buy adds a paid plan to the cart once; a second plan replaces it; free plans keep their flow

| Field | Value |
|---|---|
| Test Case ID (required) | TC-MKT-011 |
| Requirement ID (required) | [REQ-MKT-003](../../../docs/02-requirements/FRD/cart-checkout/requirement.md) (C59) |
| Acceptance Criterion | [AC-1](../../../docs/02-requirements/FRD/cart-checkout/acceptance-criteria.md), [AC-2](../../../docs/02-requirements/FRD/cart-checkout/acceptance-criteria.md), [AC-3](../../../docs/02-requirements/FRD/cart-checkout/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-MKT-003](TESTPLAN-MKT-003.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A signed-in customer; a product with Monthly and Yearly paid plans; a product with only a free plan.

## Steps
1. Click Buy on the paid product.
2. Click Buy on its Yearly plan.
3. Click Subscribe on the free-only product.

## Expected Result
Step 1: `/cart` opens with the product on its Monthly plan and the toast "Added {product} — {plan} to your cart". Step 2: the product is still in the cart once, now on Yearly. Step 3: nothing is added; the existing free-plan checkout runs. A free plan is refused by the add-to-cart API (400).

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/cart/service/CartServiceTest.java` — `buyingAddsTheProductOnceAndASecondPlanReplacesTheFirst`, `freePlansAndPlansOfAnotherProductAreRefused`
- `frontend/src/pages/CartPage.test.tsx` — "adds the product from a Buy link…", "keeps the free-plan behaviour…"
- `frontend/src/pages/ProductDetailPage.test.tsx` — "adds the product to the cart for a signed-in customer (C59)"

## Actual Result
The automated tests passed on 2026-10-01.

## Status
Passed (automated run 2026-10-01)

## Linked Defect (if failed)
None.
