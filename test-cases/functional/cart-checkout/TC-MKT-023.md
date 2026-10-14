# TC-MKT-023: An organization member's cart is submitted as one purchase request for approval

| Field | Value |
|---|---|
| Test Case ID (required) | TC-MKT-023 |
| Requirement ID (required) | [REQ-MKT-003](../../../docs/02-requirements/FRD/cart-checkout/requirement.md) (C59; changed by C100 CA-1 B and CA-2: one order, with a reason; see TC-MKT-041) |
| Acceptance Criterion | [AC-20](../../../docs/02-requirements/FRD/cart-checkout/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-MKT-003](TESTPLAN-MKT-003.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An active member of an organization who cannot buy directly (no Manage billing, not an administrator) with two items in the cart.

## Steps
1. Click Submit purchase request, give a reason and submit.

## Expected Result
**One** SUBMITTED order holding both items and the reason is created (C100: an order can hold several products); no invoice exists; the cart is empty; the Complete view shows "Order submitted for approval" with the order reference.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/cart/service/CouponAndPurchaseTest.java` — `aMembersCartBecomesOneMultiItemRequestWithAReasonAndAdminsApproveItIntoOneInvoice`
- `frontend/src/pages/CartPage.test.tsx` — "submits an organization member's cart as one purchase request with a required reason (C100 CA-2)"
- `frontend/src/pages/CheckoutPage.test.tsx` — "shows the order-submitted result for an organization member"

## Actual Result
The automated tests passed on 2026-10-14 (the tests of 2026-10-01 were replaced when the request became one multi-item order, C100).

## Status
Passed (automated run 2026-10-14)

## Linked Defect (if failed)
None.
