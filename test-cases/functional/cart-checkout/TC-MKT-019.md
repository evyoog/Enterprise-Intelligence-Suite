# TC-MKT-019: Checkout validates again and refuses with the same issues

| Field | Value |
|---|---|
| Test Case ID (required) | TC-MKT-019 |
| Requirement ID (required) | [REQ-MKT-003](../../../docs/02-requirements/FRD/cart-checkout/requirement.md) (C59) |
| Acceptance Criterion | [AC-16](../../../docs/02-requirements/FRD/cart-checkout/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-MKT-003](TESTPLAN-MKT-003.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A cart that passed validation; a product is then retired.

## Steps
1. Call `POST /api/me/cart/checkout`.

## Expected Result
409 `CART_INVALID` with the per-item issues; no subscription, invoice or order is created and the cart is unchanged.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/cart/service/CartServiceTest.java` — `validationReportsRetiredProductsChangedPricesActiveSubscriptionsAndMissingDependencies` (checkout part)

## Actual Result
The automated tests passed on 2026-10-01.

## Status
Passed (automated run 2026-10-01)

## Linked Defect (if failed)
None.
