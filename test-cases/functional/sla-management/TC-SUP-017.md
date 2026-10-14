# TC-SUP-017: A ticket created before SLA tracking has no deadlines

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SUP-017 |
| Requirement ID (required) | [REQ-SUP-002](../../../docs/02-requirements/FRD/sla-management/requirement.md) (C101) |
| Acceptance Criterion | [AC-12](../../../docs/02-requirements/FRD/sla-management/acceptance-criteria.md) |
| Business rule(s) | [BR-SLA-017](../../../docs/02-requirements/FRD/sla-management/business-rules.md) |
| Decision | [C101](../../../docs/01-business/roadmap/open-decisions.md#c101) |
| Test Plan | [TESTPLAN-SUP-002](TESTPLAN-SUP-002.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Partly |

## Preconditions
A ticket row whose SLA columns are null (a ticket created before V041).

## Steps
1. Open My tickets and the admin ticket list.
2. Change the ticket's priority as an administrator.
3. Run the SLA check (wait a minute or call it).

## Expected Result
No chips and no targets are shown; the priority change does not create deadlines; the check never warns or breaches the ticket.

## Automated coverage
- `frontend/src/pages/MyTicketsPage.test.tsx` — "shows no targets for resolved or closed tickets or tickets without deadlines"

The screen part is automated. **Not covered by an automated test:** the backend (no state, not watched, no deadlines after a priority change); the check selects only tickets with a resolution deadline and `updateTicket` recalculates only when one exists.

## Actual Result
The frontend test passed on 2026-10-14. The backend part was not run.

## Status
Partly passed: frontend automated run 2026-10-14; backend part Not Run

## Linked Defect (if failed)
None.
