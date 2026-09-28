# TC-ORD-006: A decided order cannot be cancelled

| Field | Value |
|---|---|
| Test Case ID (required) | TC-ORD-006 |
| Requirement ID (required) | [REQ-ORD-001](../../../docs/02-requirements/FRD/order-lifecycle/requirement.md) |
| Acceptance Criterion | [AC-6](../../../docs/02-requirements/FRD/order-lifecycle/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-ORD-001](TESTPLAN-ORD-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A REJECTED order.

## Steps
1. Arrange: a member submits an order; the ORG_ADMIN rejects it.
2. Act: the requester calls cancelOrder.
3. Observe the response.

## Expected Result
The request is refused (`IllegalArgumentException` / 400).

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/registration/service/OrderServiceTest.java` — `requesterCancelsOwnSubmittedOrderButNotAfterDecision`

## Actual Result
The automated tests below passed on 2026-09-28.

## Status
Passed (automated run 2026-09-28)

## Linked Defect (if failed)
None.
