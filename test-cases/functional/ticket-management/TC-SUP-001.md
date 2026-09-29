# TC-SUP-001: A customer creates and tracks their own ticket

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SUP-001 |
| Requirement ID (required) | [REQ-SUP-001](../../../docs/02-requirements/FRD/ticket-management/requirement.md) |
| Acceptance Criterion | [AC-1](../../../docs/02-requirements/FRD/ticket-management/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-SUP-001](TESTPLAN-SUP-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
None.

## Steps
1. Act: a customer calls createTicket.
2. Observe the returned status/priority.
3. Act: the same customer calls listMyTickets and getMyTicket.
4. Observe the results.

## Expected Result
The ticket is OPEN with MEDIUM priority, and appears in the customer's own list/detail.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/support/service/SupportTicketServiceTest.java` — `customerCreatesAndTracksTheirOwnTicket`

## Actual Result
The automated tests below passed on 2026-09-28.

## Status
Passed (automated run 2026-09-28)

## Linked Defect (if failed)
None.
