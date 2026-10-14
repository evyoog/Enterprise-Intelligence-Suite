# TC-ORD-011: A direct purchase is recorded as an approved order; the Orders page shows every product and the reason

| Field | Value |
|---|---|
| Test Case ID (required) | TC-ORD-011 |
| Requirement ID (required) | [REQ-ORD-001](../../../docs/02-requirements/FRD/order-lifecycle/requirement.md) (C100) |
| Acceptance Criterion | [AC-11](../../../docs/02-requirements/FRD/order-lifecycle/acceptance-criteria.md) |
| Business rule(s) | [BR-ORD-010](../../../docs/02-requirements/FRD/order-lifecycle/business-rules.md) |
| Decision | [C100](../../../docs/01-business/roadmap/open-decisions.md#c100) |
| Test Plan | [TESTPLAN-ORD-001](TESTPLAN-ORD-001.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An organization administrator and a member with Manage billing; a multi-product request with a reason and a legacy single-product order.

## Steps
1. Buy from the cart as the Manage billing member.
2. List the buyer's orders.
3. Open the Orders page as the requester and as the administrator.

## Expected Result
Step 1: an invoice is created and no request is raised. Step 2: one order for the buyer with status APPROVED. Step 3: the request appears as "Purchase request #{id}" with "Products in this request" (one line per product) and "Reason: …" for both people; the legacy order shows its single product without a list.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/cart/service/CouponAndPurchaseTest.java` — `anAdminOrAManageBillingHolderBuysDirectlyAndTheAdminsAreToldWhenItIsNotAnAdmin` (the order is APPROVED)
- `frontend/src/pages/OrganizationOrdersPage.test.tsx` — "shows every product of a purchase request and the reason, to the requester and to the admin (C100)", "still shows a single-product order without an items list"

Not covered by an automated test: the order's decision note "Purchased directly", the audit entry `ORDER_PURCHASED_DIRECTLY` and the `OrderApproved` event with `productIds`.

## Actual Result
The automated tests passed on 2026-10-14.

## Status
Passed (automated run 2026-10-14)

## Linked Defect (if failed)
None.
