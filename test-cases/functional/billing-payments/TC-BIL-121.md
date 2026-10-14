# TC-BIL-121: A coupon is checked again at invoice creation; a refused coupon creates nothing

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-121 |
| Requirement ID (required) | [REQ-BIL-001](../../../docs/02-requirements/FRD/billing-payments/requirement.md) (C100) |
| Acceptance Criterion | [AC-50](../../../docs/02-requirements/FRD/billing-payments/acceptance-criteria.md) |
| Business rule(s) | [BR-26, BR-28](../../../docs/02-requirements/FRD/billing-payments/business-rules.md) |
| Decision | [C100](../../../docs/01-business/roadmap/open-decisions.md#c100) |
| Test Plan | [TESTPLAN-BIL-001](TESTPLAN-BIL-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A cart with an applied coupon that is then disabled; a fixed coupon in another currency; a fixed coupon worth the whole cart.

## Steps
1. Disable the coupon and check out.
2. Remove the coupon and check out.
3. Apply the fixed coupon in another currency; then the one worth the whole cart.

## Expected Result
Step 1: refused with "This coupon is not active."; the cart still holds its items and the coupon's redeemed count is 0. Step 2: the invoice is created at the full price. Step 3: each is refused with its reason ("This coupon is for … purchases only.", "This coupon is worth more than your cart…").

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/cart/service/CouponAndPurchaseTest.java` — `aCouponThatStopsWorkingBeforeCheckoutIsShownInTheCartAndBlocksTheCheckoutWithNothingCreated`, `theAdministratorCreatesDisablesAndDeletesCouponsAndTheRulesAreEnforced` (the currency and worth-more refusals)

Not asserted by an automated test: that no subscription or invoice row exists after the refusal, that an expiry passing between the cart and the invoice is refused at the invoice, and that a Draft (tax-blocked) invoice still counts the redemption.

## Actual Result
The automated tests passed on 2026-10-14.

## Status
Passed (automated run 2026-10-14)

## Linked Defect (if failed)
None.
