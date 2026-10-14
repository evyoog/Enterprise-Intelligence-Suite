# TC-ORD-009: A purchase request holds several products and the requester's reason

| Field | Value |
|---|---|
| Test Case ID (required) | TC-ORD-009 |
| Requirement ID (required) | [REQ-ORD-001](../../../docs/02-requirements/FRD/order-lifecycle/requirement.md) (C100) |
| Acceptance Criterion | [AC-9](../../../docs/02-requirements/FRD/order-lifecycle/acceptance-criteria.md) |
| Business rule(s) | [BR-ORD-007, BR-ORD-008, BR-ORD-011](../../../docs/02-requirements/FRD/order-lifecycle/business-rules.md) |
| Decision | [C100](../../../docs/01-business/roadmap/open-decisions.md#c100) |
| Test Plan | [TESTPLAN-ORD-001](TESTPLAN-ORD-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An organization with an administrator and a plain member; two ACTIVE products with plans.

## Steps
1. As the member, build a cart of both products and submit it with a blank reason, then with a reason.
2. List the member's orders.
3. Read the administrator's notifications.

## Expected Result
Step 1: the blank reason is refused ("Tell your organization admin why you need this…") and nothing is created; with a reason **one** order is created with status SUBMITTED, holding both products and the reason, and no invoice exists. Step 2: the order has two `items` (in order) and `requestReason`. Step 3: the administrator has "New purchase request awaiting approval" with the reason.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/cart/service/CouponAndPurchaseTest.java` — `aMembersCartBecomesOneMultiItemRequestWithAReasonAndAdminsApproveItIntoOneInvoice`

Not covered by an automated test: the 500-character limit, "Each product can be in an order once." and "Choose at least one product." at the order API.

## Actual Result
The automated tests passed on 2026-10-14.

## Status
Passed (automated run 2026-10-14)

## Linked Defect (if failed)
None.
