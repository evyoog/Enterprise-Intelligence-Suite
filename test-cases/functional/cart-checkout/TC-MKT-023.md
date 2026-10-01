# TC-MKT-023: An organization member's cart is submitted as orders for approval

| Field | Value |
|---|---|
| Test Case ID (required) | TC-MKT-023 |
| Requirement ID (required) | [REQ-MKT-003](../../../docs/02-requirements/FRD/cart-checkout/requirement.md) (C59) |
| Acceptance Criterion | [AC-20](../../../docs/02-requirements/FRD/cart-checkout/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-MKT-003](TESTPLAN-MKT-003.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An active member of an organization with two items in the cart.

## Steps
1. Click Submit order for approval.

## Expected Result
One SUBMITTED order per item is created (REQ-ORD-001 orders hold one product); no invoice exists; the cart is empty; the Complete view shows "Order submitted for approval" with the order references.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/cart/service/CartServiceTest.java` — `anOrganizationMembersCheckoutSubmitsOneOrderPerItemAndNoInvoice`
- `frontend/src/pages/CartPage.test.tsx` — "submits an organization member's cart for approval"
- `frontend/src/pages/CheckoutPage.test.tsx` — "shows the order-submitted result for an organization member"

## Actual Result
The automated tests passed on 2026-10-01.

## Status
Passed (automated run 2026-10-01)

## Linked Defect (if failed)
None.
