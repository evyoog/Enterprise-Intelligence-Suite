# TC-MKT-046: A purchase request carries no coupon

| Field | Value |
|---|---|
| Test Case ID (required) | TC-MKT-046 |
| Requirement ID (required) | [REQ-MKT-003](../../../docs/02-requirements/FRD/cart-checkout/requirement.md) (C100) |
| Acceptance Criterion | [AC-35](../../../docs/02-requirements/FRD/cart-checkout/acceptance-criteria.md) |
| Business rule(s) | [BR-18](../../../docs/02-requirements/FRD/cart-checkout/business-rules.md) |
| Decision | [C100](../../../docs/01-business/roadmap/open-decisions.md#c100) |
| Test Plan | [TESTPLAN-MKT-003](TESTPLAN-MKT-003.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A cart of an organization member who cannot buy directly; an active coupon.

## Steps
1. Open the cart.
2. Call `PUT /me/cart/coupon` with the coupon's code.

## Expected Result
Step 1: no coupon area is shown. Step 2: 400 "A coupon cannot be used on a purchase request. An organization admin can use it when they buy."

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/cart/service/CouponAndPurchaseTest.java` — `aMembersCartBecomesOneMultiItemRequestWithAReasonAndAdminsApproveItIntoOneInvoice` (the refusal)
- `frontend/src/pages/CartPage.test.tsx` — "submits an organization member's cart as one purchase request with a required reason (C100 CA-2)" (no coupon field)

## Actual Result
The automated tests passed on 2026-10-14.

## Status
Passed (automated run 2026-10-14)

## Linked Defect (if failed)
None.
