# TC-ORD-010: Approving a multi-product order provisions every product and creates one invoice

| Field | Value |
|---|---|
| Test Case ID (required) | TC-ORD-010 |
| Requirement ID (required) | [REQ-ORD-001](../../../docs/02-requirements/FRD/order-lifecycle/requirement.md) (C100) |
| Acceptance Criterion | [AC-10](../../../docs/02-requirements/FRD/order-lifecycle/acceptance-criteria.md) |
| Business rule(s) | [BR-ORD-002, BR-ORD-009](../../../docs/02-requirements/FRD/order-lifecycle/business-rules.md) |
| Decision | [C100](../../../docs/01-business/roadmap/open-decisions.md#c100) |
| Test Plan | [TESTPLAN-ORD-001](TESTPLAN-ORD-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A SUBMITTED order with two products (TC-ORD-009).

## Steps
1. As the administrator (MANAGE_ORDERS), approve the order.
2. Read the organization's invoices.

## Expected Result
The order is APPROVED; both products have an organization subscription; the organization has exactly **one** invoice, with two lines.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/cart/service/CouponAndPurchaseTest.java` — `aMembersCartBecomesOneMultiItemRequestWithAReasonAndAdminsApproveItIntoOneInvoice` (after `approveOrder`)

Not covered by an automated test: the single-product path creating its invoice the old way (`OrderServiceTest.memberSubmitsOrderThenAdminApprovesAndProvisions` asserts the provisioning, not the invoice), and a request with items that are all free (no invoice).

## Actual Result
The automated tests passed on 2026-10-14.

## Status
Passed (automated run 2026-10-14)

## Linked Defect (if failed)
None.
