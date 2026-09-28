# TC-SUP-003: Assigning an open ticket moves it to in-progress

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SUP-003 |
| Requirement ID (required) | [REQ-SUP-001](../../../docs/02-requirements/FRD/ticket-management/requirement.md) |
| Acceptance Criterion | [AC-3](../../../docs/02-requirements/FRD/ticket-management/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-SUP-001](TESTPLAN-SUP-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An OPEN ticket.

## Steps
1. Arrange: a customer creates a ticket.
2. Act: an admin calls updateTicket with a category, priority and assignee.
3. Observe the returned status/category/priority/assignee.

## Expected Result
The ticket becomes IN_PROGRESS with the given category, priority and assignee.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/support/service/SupportTicketServiceTest.java` — `assigningAnOpenTicketMovesItToInProgress`

## Actual Result
The automated tests below passed on 2026-09-28.

## Status
Passed (automated run 2026-09-28)

## Linked Defect (if failed)
None.
