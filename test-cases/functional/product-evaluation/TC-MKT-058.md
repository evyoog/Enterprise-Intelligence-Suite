# TC-MKT-058: Buying through the cart: after the trial has ended, and while it is still running

| Field | Value |
|---|---|
| Test Case ID (required) | TC-MKT-058 |
| Requirement ID (required) | [REQ-MKT-005](../../../docs/02-requirements/FRD/product-evaluation/requirement.md) (C102) |
| Acceptance Criterion | [AC-10](../../../docs/02-requirements/FRD/product-evaluation/acceptance-criteria.md) |
| Business rule(s) | [BR-EVL-015, BR-EVL-016](../../../docs/02-requirements/FRD/product-evaluation/business-rules.md) |
| Decision | [C102](../../../docs/01-business/roadmap/open-decisions.md#c102) |
| Test Plan | [TESTPLAN-MKT-005](TESTPLAN-MKT-005.md) |
| Priority | P0 |
| Type | Regression |
| Automated | Yes |

## Preconditions
Two customers, each with a trial: one whose trial has ended (EXPIRED) and one whose trial is still running (ACTIVE, `trial = true`); a paid plan of the product in each cart.

## Steps
1. Customer 1: add the paid plan to the cart, check the cart (`POST /me/cart/validate`) and check out.
2. Customer 2: add the paid plan to the cart and check the cart.

## Expected Result
Customer 1: no cart issue; checkout creates the invoice and the subscription becomes a normal one (PENDING_SUBSCRIPTION until paid). Customer 2 (C102, "buying during or after the trial"): the cart reports no `ALREADY_SUBSCRIBED` issue for the trial.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/cart/service/CartServiceTest.java` — `aRunningFreeTrialDoesNotBlockBuyingTheSameProduct`

`CartService#hasActiveSubscription` now excludes a subscription with `trial = true` from `ALREADY_SUBSCRIBED`, so a running trial reaches checkout like any other product.

## Actual Result
The automated test passed on 2026-10-15.

## Status
Passed (automated run 2026-10-15)

## Linked Defect (if failed)
None.
