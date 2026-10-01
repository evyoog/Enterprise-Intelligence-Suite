# TC-MKT-018: Purchase validation reports each issue in its item and blocks checkout

| Field | Value |
|---|---|
| Test Case ID (required) | TC-MKT-018 |
| Requirement ID (required) | [REQ-MKT-003](../../../docs/02-requirements/FRD/cart-checkout/requirement.md) (C59) |
| Acceptance Criterion | [AC-12](../../../docs/02-requirements/FRD/cart-checkout/acceptance-criteria.md), [AC-13](../../../docs/02-requirements/FRD/cart-checkout/acceptance-criteria.md), [AC-14](../../../docs/02-requirements/FRD/cart-checkout/acceptance-criteria.md), [AC-15](../../../docs/02-requirements/FRD/cart-checkout/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-MKT-003](TESTPLAN-MKT-003.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A cart with: a product retired after it was added; a plan whose price rose from 1,000.00 to 1,200.00; a product the customer already has ACTIVE; a product that depends on one the customer neither has nor has in the cart.

## Steps
1. Click Proceed to checkout.
2. Click Confirm new price on the price issue.
3. Add the required product to the cart and validate again.

## Expected Result
Step 1: each item shows its message ("This product is no longer available.", "The price changed from 1,000.00 to 1,200.00.", "You already have an active subscription to {product}.", "{product} requires {required}.") and the customer stays on the cart; no invoice or order is created. Step 2: the price issue clears. Step 3: a required product in the same cart is not an issue.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/cart/service/CartServiceTest.java` — `validationReportsRetiredProductsChangedPricesActiveSubscriptionsAndMissingDependencies`, `confirmingTheNewPriceClearsThePriceIssue`, `aRequiredProductInTheSameCartIsNotAMissingDependency`
- `frontend/src/pages/CartPage.test.tsx` — "keeps the customer on the cart and shows each validation issue in its item"

## Actual Result
The automated tests passed on 2026-10-01.

## Status
Passed (automated run 2026-10-01)

## Linked Defect (if failed)
None.
