# TC-MKT-040: An organization administrator or Manage billing holder buys directly with one invoice

| Field | Value |
|---|---|
| Test Case ID (required) | TC-MKT-040 |
| Requirement ID (required) | [REQ-MKT-003](../../../docs/02-requirements/FRD/cart-checkout/requirement.md) (C100) |
| Acceptance Criterion | [AC-29](../../../docs/02-requirements/FRD/cart-checkout/acceptance-criteria.md) |
| Business rule(s) | [BR-13, BR-14](../../../docs/02-requirements/FRD/cart-checkout/business-rules.md) |
| Decision | [C100](../../../docs/01-business/roadmap/open-decisions.md#c100) |
| Test Plan | [TESTPLAN-MKT-003](TESTPLAN-MKT-003.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An organization with two administrators and a member who holds Manage billing; two paid products (40.00 and 60.00); billing details saved.

## Steps
1. As an administrator, add a product to the cart and read the primary button.
2. Proceed to checkout.
3. As the Manage billing member, add two products, apply a 50 % coupon and proceed.
4. Read the notifications of the administrators.

## Expected Result
Step 1: the cart's `continueAs` is `ORGANIZATION_BUYER`; the button is **Proceed to checkout** with the note "If you are not an organization admin, the organization admins are notified of this purchase."; no reason is asked. Step 2: the result is an invoice (the checkout opens for it) and no dialog; the other administrator is **not** notified. Step 3: one invoice with two lines whose subtotal is the cart total (half of (40.00 + 60.00) × 5 seats), the coupon code on it, the order recorded as APPROVED by the buyer. Step 4: every administrator has "A purchase was made for your organization" naming the product.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/cart/service/CouponAndPurchaseTest.java` — `anAdminOrAManageBillingHolderBuysDirectlyAndTheAdminsAreToldWhenItIsNotAnAdmin`
- `frontend/src/pages/CartPage.test.tsx` — "lets an organization buyer purchase directly, with a note that the admins are notified (C100 CA-2)"

## Actual Result
The automated tests passed on 2026-10-14.

## Status
Passed (automated run 2026-10-14)

## Linked Defect (if failed)
None.
