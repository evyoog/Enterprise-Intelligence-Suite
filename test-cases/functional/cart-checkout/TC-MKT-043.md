# TC-MKT-043: An organization item shows seats × price; the amount and subtotal follow

| Field | Value |
|---|---|
| Test Case ID (required) | TC-MKT-043 |
| Requirement ID (required) | [REQ-MKT-003](../../../docs/02-requirements/FRD/cart-checkout/requirement.md) (C100) |
| Acceptance Criterion | [AC-32](../../../docs/02-requirements/FRD/cart-checkout/acceptance-criteria.md) |
| Business rule(s) | [BR-17](../../../docs/02-requirements/FRD/cart-checkout/business-rules.md) |
| Decision | [C100](../../../docs/01-business/roadmap/open-decisions.md#c100) |
| Test Plan | [TESTPLAN-MKT-003](TESTPLAN-MKT-003.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An organization with 5 seats on a product priced 1,000.00 per seat and another product the organization has no subscription to; an individual cart.

## Steps
1. Open the cart as an organization buyer.
2. Read each item.
3. Open the cart as an individual.

## Expected Result
The first item reads "5 seats × 1,000.00" with the amount 5,000.00; the subtotal is the sum of the amounts; an item for one seat shows no seats line; an individual's items are one seat and show none.

## Automated coverage
- `frontend/src/pages/CartPage.test.tsx` — "shows the seats and the per-seat price of an organization item, and nothing for one seat"
- `backend/src/test/java/com/vyoog/eisplatform/modules/cart/service/CouponAndPurchaseTest.java` — `anAdminOrAManageBillingHolderBuysDirectlyAndTheAdminsAreToldWhenItIsNotAnAdmin` (each item has the organization's 5 seats; subtotal (4,000 + 6,000) × 5)

Not covered by an automated test: that the seats come from an existing subscription to the product rather than the licensed seats (`SubscriptionService.initialSeats`), and the quantity on the invoice line of the purchase. Per-seat pricing is an engineering default to confirm.

## Actual Result
The automated tests passed on 2026-10-14.

## Status
Passed (automated run 2026-10-14)

## Linked Defect (if failed)
None.
