# TC-BIL-120: A coupon takes the discount off before tax and is counted once the invoice exists

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-120 |
| Requirement ID (required) | [REQ-BIL-001](../../../docs/02-requirements/FRD/billing-payments/requirement.md) (C100) |
| Acceptance Criterion | [AC-49](../../../docs/02-requirements/FRD/billing-payments/acceptance-criteria.md) |
| Business rule(s) | [BR-27, BR-28](../../../docs/02-requirements/FRD/billing-payments/business-rules.md) |
| Decision | [C100](../../../docs/01-business/roadmap/open-decisions.md#c100) |
| Test Plan | [TESTPLAN-BIL-001](TESTPLAN-BIL-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A customer with billing details in India and approved tax rules; two items in the cart (100.00 and 250.50); a 10 % coupon.

## Steps
1. Apply the coupon (lower case) and read the cart.
2. Check out.
3. Read the invoice, its lines and its tax lines.
4. Read the checkout summary.
5. Try to delete the coupon.

## Expected Result
Step 1: subtotal 350.50, discount 35.05, total 315.45. Step 3: one OPEN invoice, `discountAmount` 35.05 and the code, `subtotal` 315.45, the lines' discounts add up to 35.05, tax is calculated on 315.45, total = subtotal + tax; the coupon's redeemed count is 1; the cart is empty and has no coupon. Step 4: the summary's `subtotal` is 350.50 (gross), with the discount, the code and the same total; the screen shows "Discount (CODE)" with a minus amount between the subtotal and the tax. Step 5: 409 "A coupon that has been redeemed cannot be deleted. Disable it instead."

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/cart/service/CouponAndPurchaseTest.java` — `aCouponTakesTheDiscountOffBeforeTaxAndIsCountedOnceTheInvoiceExists`
- `frontend/src/pages/CheckoutPage.test.tsx` — "shows the coupon discount between the gross subtotal and the tax, and the net total (C100)"

## Actual Result
The automated tests passed on 2026-10-14.

## Status
Passed (automated run 2026-10-14)

## Linked Defect (if failed)
None.
