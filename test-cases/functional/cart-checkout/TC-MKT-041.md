# TC-MKT-041: A member's cart becomes one multi-item purchase request with a reason; approval creates one invoice

| Field | Value |
|---|---|
| Test Case ID (required) | TC-MKT-041 |
| Requirement ID (required) | [REQ-MKT-003](../../../docs/02-requirements/FRD/cart-checkout/requirement.md) (C100) |
| Acceptance Criterion | [AC-30](../../../docs/02-requirements/FRD/cart-checkout/acceptance-criteria.md) |
| Business rule(s) | [BR-15, BR-16](../../../docs/02-requirements/FRD/cart-checkout/business-rules.md) |
| Decision | [C100](../../../docs/01-business/roadmap/open-decisions.md#c100) |
| Test Plan | [TESTPLAN-MKT-003](TESTPLAN-MKT-003.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An organization with an administrator and a plain member; two paid products in the member's cart; a coupon that exists.

## Steps
1. As the member, read the primary button and try to apply a coupon.
2. Click **Submit purchase request**; submit the dialog with a blank reason, then with a reason.
3. Read the administrator's notifications and the member's orders.
4. As the administrator, approve the request.

## Expected Result
Step 1: `continueAs` is `ORGANIZATION_MEMBER`; the button reads **Submit purchase request** with the text "You cannot buy products yourself…"; there is no coupon field and the API refuses a code ("A coupon cannot be used on a purchase request…"). Step 2: a dialog "Submit a purchase request" asks "Why do you need this?"; a blank reason shows "Tell your organization admin why you need this." and nothing is sent (the API also refuses it); the dialog counts up to 500 characters. With a reason, **one** order is created with both products, the reason and status SUBMITTED, the cart is empty and the customer lands on the Complete step (`/checkout?orders={id}&step=complete`). Step 3: the administrator is notified ("New purchase request awaiting approval") with the reason; no invoice exists. Step 4: both products are provisioned, **one** invoice with two lines exists and the order is APPROVED.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/cart/service/CouponAndPurchaseTest.java` — `aMembersCartBecomesOneMultiItemRequestWithAReasonAndAdminsApproveItIntoOneInvoice`
- `frontend/src/pages/CartPage.test.tsx` — "submits an organization member's cart as one purchase request with a required reason (C100 CA-2)", "does not ask for a reason when the cart has validation issues"
- `frontend/src/pages/CheckoutPage.test.tsx` — "shows the order-submitted result for an organization member"
- `frontend/src/pages/OrganizationOrdersPage.test.tsx` — "shows every product of a purchase request and the reason, to the requester and to the admin (C100)"

Not covered by an automated test: the 500-character limit on the server (only the field's `maxlength` is asserted).

## Actual Result
The automated tests passed on 2026-10-14.

## Status
Passed (automated run 2026-10-14)

## Linked Defect (if failed)
None.
