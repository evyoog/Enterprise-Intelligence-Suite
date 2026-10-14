# TC-MKT-044: A coupon code is applied in the cart, shown as a discount and removed

| Field | Value |
|---|---|
| Test Case ID (required) | TC-MKT-044 |
| Requirement ID (required) | [REQ-MKT-003](../../../docs/02-requirements/FRD/cart-checkout/requirement.md) (C100) |
| Acceptance Criterion | [AC-33](../../../docs/02-requirements/FRD/cart-checkout/acceptance-criteria.md) |
| Business rule(s) | [BR-18](../../../docs/02-requirements/FRD/cart-checkout/business-rules.md) |
| Decision | [C100](../../../docs/01-business/roadmap/open-decisions.md#c100) |
| Test Plan | [TESTPLAN-MKT-003](TESTPLAN-MKT-003.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A cart of two items (100.00 and 250.50); a 10 % coupon and an expired coupon.

## Steps
1. Type "save10" in the coupon field and click **Apply**.
2. Read the summary; click **Remove coupon**.
3. Apply the expired coupon.

## Expected Result
Step 1: the code is sent in capitals; the summary shows "Discount (SAVE10)" with a minus amount, a total net of it (subtotal 350.50, discount 35.05, total 315.45) and the chip "Coupon SAVE10" with **Remove coupon**; the field is gone. Step 2: the field and the full total come back. Step 3: "This coupon has expired." is shown under the field and the typed text stays.

## Automated coverage
- `frontend/src/pages/CartPage.test.tsx` — "applies a coupon code in capitals and shows the discount", "shows a coupon on the summary with a negative discount line and the net total, and removes it", "shows the reason a coupon is refused and keeps the field"
- `backend/src/test/java/com/vyoog/eisplatform/modules/cart/service/CouponAndPurchaseTest.java` — `aCouponTakesTheDiscountOffBeforeTaxAndIsCountedOnceTheInvoiceExists` (the cart's subtotal, discount and total)

## Actual Result
The automated tests passed on 2026-10-14.

## Status
Passed (automated run 2026-10-14)

## Linked Defect (if failed)
None.
