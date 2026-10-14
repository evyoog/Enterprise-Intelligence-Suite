# TC-SUP-029: The problem list shows counts of incidents and open actions

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SUP-029 |
| Requirement ID (required) | [REQ-SUP-003](../../../docs/02-requirements/FRD/incident-problem-management/requirement.md) (C101) |
| Acceptance Criterion | [AC-9](../../../docs/02-requirements/FRD/incident-problem-management/acceptance-criteria.md) |
| Business rule(s) | [BR-INC-015](../../../docs/02-requirements/FRD/incident-problem-management/business-rules.md) |
| Decision | [C101](../../../docs/01-business/roadmap/open-decisions.md#c101) |
| Test Plan | [TESTPLAN-SUP-003](TESTPLAN-SUP-003.md) |
| Priority | P2 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A problem with one incident and two actions, one done.

## Steps
1. Read the problem list (API and screen).

## Expected Result
1 incident, 2 actions, 1 open action ("1 open / 2" on the screen).

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/servicestatus/service/IncidentAndProblemTest.java` — `theProblemListShowsCountsOfIncidentsAndOpenActions`
- `frontend/src/pages/admin/AdminProblemsPage.test.tsx` — "lists problems with status, incidents and open/total actions, and has no accessibility violations"

## Actual Result
The automated tests above passed on 2026-10-14.

## Status
Passed (automated run 2026-10-14)

## Linked Defect (if failed)
None.
