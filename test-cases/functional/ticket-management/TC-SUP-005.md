# TC-SUP-005: Resolve then close follows the one-way lifecycle

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SUP-005 |
| Requirement ID (required) | [REQ-SUP-001](../../../docs/02-requirements/FRD/ticket-management/requirement.md) |
| Acceptance Criterion | [AC-5/AC-6](../../../docs/02-requirements/FRD/ticket-management/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-SUP-001](TESTPLAN-SUP-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An OPEN ticket.

## Steps
1. Act: attempt to close the ticket before it is resolved.
2. Act: resolve the ticket with a note.
3. Act: close the resolved ticket.
4. Act: attempt updateTicket/escalateTicket/resolveTicket again on the closed ticket.
5. Observe each response.

## Expected Result
Step 1 is refused (400). Step 2 succeeds (RESOLVED, resolvedAt set). Step 3 succeeds (CLOSED, closedAt set). Every action in step 4 is refused (400).

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/support/service/SupportTicketServiceTest.java` — `resolveThenCloseFollowsTheOneWayLifecycle`

## Actual Result
The automated tests below passed on 2026-09-28.

## Status
Passed (automated run 2026-09-28)

## Linked Defect (if failed)
None.
