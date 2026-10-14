# TC-SUP-032: The Incidents tab: list, create, edit by phase, link tickets, closed read-only

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SUP-032 |
| Requirement ID (required) | [REQ-SUP-003](../../../docs/02-requirements/FRD/incident-problem-management/requirement.md) (C101) |
| Acceptance Criterion | [AC-12](../../../docs/02-requirements/FRD/incident-problem-management/acceptance-criteria.md) |
| Business rule(s) | [BR-INC-003 to BR-INC-009](../../../docs/02-requirements/FRD/incident-problem-management/business-rules.md) |
| Decision | [C101](../../../docs/01-business/roadmap/open-decisions.md#c101) |
| Test Plan | [TESTPLAN-SUP-003](TESTPLAN-SUP-003.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A platform administrator on Platform → Service status → Incidents.

## Steps
1. Open the tab; open it when the API fails; open it with no incidents.
2. Create an incident from the public and internal groups.
3. Edit an incident: look at the phases offered, try RESOLVED without a summary, and let the server refuse a change.
4. Open a resolved and a closed incident; link and unlink a ticket.

## Expected Result
Open incidents with severity, phase, assignee, tickets and problem (closed hidden by default, no axe violations); a retry; an empty state with New incident; the new incident is sent with its public and internal fields; only the allowed next phases are offered and a summary is required; the server's refusal keeps the dialog open; reopen sends no end time; a closed incident is read-only; tickets link and unlink.

## Automated coverage
- `frontend/src/pages/admin/AdminIncidentsPage.test.tsx` — "lists the open incidents with severity, phase, assignee, tickets and problem, hides closed by default, and has no accessibility violations", "offers a retry when the incidents cannot be loaded", "shows an empty state with a New incident action", "creates an incident from the public and internal groups", "only offers the allowed next phases and requires a resolution summary before RESOLVED", "shows what the server refuses and keeps the dialog open", "offers reopening a resolved incident and sends no end time", "shows a closed incident read-only", "links a picked support ticket and unlinks a linked one", "lets an administrator without ticket access type a ticket id"

## Actual Result
The automated tests above passed on 2026-10-14.

## Status
Passed (automated run 2026-10-14)

## Linked Defect (if failed)
None.
