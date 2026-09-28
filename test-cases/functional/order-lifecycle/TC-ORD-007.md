# TC-ORD-007: The requester cancels their own submitted order

| Field | Value |
|---|---|
| Test Case ID (required) | TC-ORD-007 |
| Requirement ID (required) | [REQ-ORD-001](../../../docs/02-requirements/FRD/order-lifecycle/requirement.md) |
| Acceptance Criterion | [AC-7](../../../docs/02-requirements/FRD/order-lifecycle/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-ORD-001](TESTPLAN-ORD-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A SUBMITTED order.

## Steps
1. Arrange: a member submits an order.
2. Act: the same member calls cancelOrder.
3. Observe the returned status.

## Expected Result
The order becomes CANCELLED.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/registration/service/OrderServiceTest.java` — `requesterCancelsOwnSubmittedOrderButNotAfterDecision`

## Actual Result
The automated tests below passed on 2026-09-28.

## Status
Passed (automated run 2026-09-28)

## Linked Defect (if failed)
None.
