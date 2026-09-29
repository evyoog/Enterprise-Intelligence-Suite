# TC-ORD-005: ORG_ADMIN rejects an order with a note

| Field | Value |
|---|---|
| Test Case ID (required) | TC-ORD-005 |
| Requirement ID (required) | [REQ-ORD-001](../../../docs/02-requirements/FRD/order-lifecycle/requirement.md) |
| Acceptance Criterion | [AC-5](../../../docs/02-requirements/FRD/order-lifecycle/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-ORD-001](TESTPLAN-ORD-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A SUBMITTED order.

## Steps
1. Arrange: a member submits an order.
2. Act: the ORG_ADMIN calls rejectOrder with a note.
3. Observe the returned status and note.

## Expected Result
The order becomes REJECTED and the note is recorded.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/registration/service/OrderServiceTest.java` — `rejectingAnOrderRecordsTheDecisionNote`

## Actual Result
The automated tests below passed on 2026-09-28.

## Status
Passed (automated run 2026-09-28)

## Linked Defect (if failed)
None.
