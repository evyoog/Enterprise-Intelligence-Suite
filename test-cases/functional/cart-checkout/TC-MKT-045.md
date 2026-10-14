# TC-MKT-045: A coupon that stops working before checkout blocks it and creates nothing

| Field | Value |
|---|---|
| Test Case ID (required) | TC-MKT-045 |
| Requirement ID (required) | [REQ-MKT-003](../../../docs/02-requirements/FRD/cart-checkout/requirement.md) (C100) |
| Acceptance Criterion | [AC-34](../../../docs/02-requirements/FRD/cart-checkout/acceptance-criteria.md) |
| Business rule(s) | [BR-19](../../../docs/02-requirements/FRD/cart-checkout/business-rules.md) |
| Decision | [C100](../../../docs/01-business/roadmap/open-decisions.md#c100) |
| Test Plan | [TESTPLAN-MKT-003](TESTPLAN-MKT-003.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A cart with an applied coupon that an administrator then disables.

## Steps
1. Open the cart.
2. Click Proceed to checkout.
3. Click **Remove coupon** and proceed again.

## Expected Result
Step 1: the code stays visible with the warning "This coupon is not active.", no discount line, and the total is the full amount. Step 2: checkout is refused with the same message; the cart still holds its items; the coupon's redeemed count is 0. Step 3: checkout succeeds and creates the invoice at the full price.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/cart/service/CouponAndPurchaseTest.java` — `aCouponThatStopsWorkingBeforeCheckoutIsShownInTheCartAndBlocksTheCheckoutWithNothingCreated`
- `frontend/src/pages/CartPage.test.tsx` — "warns when the applied coupon can no longer be used, and lets the customer remove it", "keeps the cart and shows the message when checkout is refused because the coupon became invalid"

Not asserted by an automated test: that no subscription or invoice row exists after the refusal (the transaction rolls back; the untouched cart and the redeemed count of 0 are asserted).

## Actual Result
The automated tests passed on 2026-10-14.

## Status
Passed (automated run 2026-10-14)

## Linked Defect (if failed)
None.
