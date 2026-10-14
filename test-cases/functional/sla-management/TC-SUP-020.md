# TC-SUP-020: Regression: the ticket actions of REQ-SUP-001 still work with the SLA fields

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SUP-020 |
| Requirement ID (required) | [REQ-SUP-002](../../../docs/02-requirements/FRD/sla-management/requirement.md) (C101) |
| Acceptance Criterion | [AC-15](../../../docs/02-requirements/FRD/sla-management/acceptance-criteria.md) |
| Business rule(s) | [BR-SUP-001 to BR-SUP-008](../../../docs/02-requirements/FRD/sla-management/business-rules.md) |
| Decision | [C101](../../../docs/01-business/roadmap/open-decisions.md#c101) |
| Test Plan | [TESTPLAN-SUP-002](TESTPLAN-SUP-002.md) |
| Priority | P0 |
| Type | Regression |
| Automated | Yes |

## Preconditions
The existing ticket tests ([TESTPLAN-SUP-001](../ticket-management/TESTPLAN-SUP-001.md)).

## Steps
1. Run `SupportTicketServiceTest` and `AdminSupportTicketsPage.test.tsx` unchanged.

## Expected Result
Create, ownership, assign, escalate (priority URGENT), resolve and close, and the admin actions escalate, resolve and close, behave as before.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/support/service/SupportTicketServiceTest.java` — `customerCreatesAndTracksTheirOwnTicket`, `anotherCustomersTicketIsInvisible`, `assigningAnOpenTicketMovesItToInProgress`, `escalatingRaisesPriorityToUrgent`, `resolveThenCloseFollowsTheOneWayLifecycle`
- `frontend/src/pages/admin/AdminSupportTicketsPage.test.tsx` — "escalates a ticket", "resolves a ticket", "closes a resolved ticket"

## Actual Result
The automated tests above passed on 2026-10-14.

## Status
Passed (automated run 2026-10-14)

## Linked Defect (if failed)
None.
