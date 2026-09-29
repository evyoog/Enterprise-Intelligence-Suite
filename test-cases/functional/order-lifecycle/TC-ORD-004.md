# TC-ORD-004: A plain member cannot approve an order

| Field | Value |
|---|---|
| Test Case ID (required) | TC-ORD-004 |
| Requirement ID (required) | [REQ-ORD-001](../../../docs/02-requirements/FRD/order-lifecycle/requirement.md) |
| Acceptance Criterion | [AC-4](../../../docs/02-requirements/FRD/order-lifecycle/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-ORD-001](TESTPLAN-ORD-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A SUBMITTED order; a member with no MANAGE_ORDERS permission.

## Steps
1. Arrange: a member submits an order.
2. Act: the same member (not an ORG_ADMIN) calls approveOrder.
3. Observe the response.

## Expected Result
The request is refused (`ForbiddenException` / 403).

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/registration/service/OrderServiceTest.java` — `onlyAnAdminMayApprove`

## Actual Result
The automated tests below passed on 2026-09-28.

## Status
Passed (automated run 2026-09-28)

## Linked Defect (if failed)
None.
