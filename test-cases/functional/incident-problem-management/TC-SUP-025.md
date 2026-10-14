# TC-SUP-025: Assignee and linked tickets

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SUP-025 |
| Requirement ID (required) | [REQ-SUP-003](../../../docs/02-requirements/FRD/incident-problem-management/requirement.md) (C101) |
| Acceptance Criterion | [AC-5](../../../docs/02-requirements/FRD/incident-problem-management/acceptance-criteria.md) |
| Business rule(s) | [BR-INC-008, BR-INC-009](../../../docs/02-requirements/FRD/incident-problem-management/business-rules.md) |
| Decision | [C101](../../../docs/01-business/roadmap/open-decisions.md#c101) |
| Test Plan | [TESTPLAN-SUP-003](TESTPLAN-SUP-003.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An agent, a requester with a ticket, and an incident.

## Steps
1. Log an incident assigned to the agent.
2. Link the ticket; link it again; unlink it.
3. Link ticket -5; log an incident with assignee -9.
4. On the screen link a picked ticket, unlink it, and (as an administrator without ticket access) type a ticket id.

## Expected Result
The assignee name is shown; the ticket is listed with its subject, status and priority, linking twice changes nothing, unlinking empties the list; an unknown ticket is 404 and an unknown assignee is refused; the screen links and unlinks by picker or by typed id.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/servicestatus/service/IncidentAndProblemTest.java` — `assigneeAndTicketsAreLinkedAndAMissingOneIsRejected`
- `frontend/src/pages/admin/AdminIncidentsPage.test.tsx` — "links a picked support ticket and unlinks a linked one", "lets an administrator without ticket access type a ticket id"

## Actual Result
The automated tests above passed on 2026-10-14.

## Status
Passed (automated run 2026-10-14)

## Linked Defect (if failed)
None.
