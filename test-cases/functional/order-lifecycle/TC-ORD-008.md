# TC-ORD-008: Another organization's order is invisible

| Field | Value |
|---|---|
| Test Case ID (required) | TC-ORD-008 |
| Requirement ID (required) | [REQ-ORD-001](../../../docs/02-requirements/FRD/order-lifecycle/requirement.md) |
| Acceptance Criterion | [AC-8](../../../docs/02-requirements/FRD/order-lifecycle/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-ORD-001](TESTPLAN-ORD-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
Two organizations, each with its own admin and member.

## Steps
1. Arrange: organization A's member submits an order.
2. Act: organization B's ORG_ADMIN calls approveOrder on organization A's order id.
3. Observe the response.

## Expected Result
The request is refused with a generic 404 (`ResourceNotFoundException`), never confirming the order exists.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/registration/service/OrderServiceTest.java` — `anotherOrganizationsOrderIsInvisible`

## Actual Result
The automated tests below passed on 2026-09-28.

## Status
Passed (automated run 2026-09-28)

## Linked Defect (if failed)
None.
