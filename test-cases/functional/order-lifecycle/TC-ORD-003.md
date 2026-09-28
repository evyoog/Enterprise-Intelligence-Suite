# TC-ORD-003: ORG_ADMIN approves and the order provisions the subscription

| Field | Value |
|---|---|
| Test Case ID (required) | TC-ORD-003 |
| Requirement ID (required) | [REQ-ORD-001](../../../docs/02-requirements/FRD/order-lifecycle/requirement.md) |
| Acceptance Criterion | [AC-3](../../../docs/02-requirements/FRD/order-lifecycle/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-ORD-001](TESTPLAN-ORD-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A SUBMITTED order.

## Steps
1. Arrange: a member submits an order with a plan.
2. Act: the org's ORG_ADMIN calls approveOrder.
3. Observe the order status and the organization's subscription.

## Expected Result
The order becomes APPROVED, and the organization now has an ACTIVE subscription to that product with the chosen plan.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/registration/service/OrderServiceTest.java` — `memberSubmitsOrderThenAdminApprovesAndProvisions`

## Actual Result
The automated tests below passed on 2026-09-28.

## Status
Passed (automated run 2026-09-28)

## Linked Defect (if failed)
None.
