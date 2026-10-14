# TC-SUP-028: A closed problem takes no more incidents; a problem needs a title

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SUP-028 |
| Requirement ID (required) | [REQ-SUP-003](../../../docs/02-requirements/FRD/incident-problem-management/requirement.md) (C101) |
| Acceptance Criterion | [AC-8](../../../docs/02-requirements/FRD/incident-problem-management/acceptance-criteria.md) |
| Business rule(s) | [BR-INC-010, BR-INC-013](../../../docs/02-requirements/FRD/incident-problem-management/business-rules.md) |
| Decision | [C101](../../../docs/01-business/roadmap/open-decisions.md#c101) |
| Test Plan | [TESTPLAN-SUP-003](TESTPLAN-SUP-003.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A problem resolved with a root cause and closed.

## Steps
1. Link an incident to it.
2. Create a problem with a blank title.
3. Read problem -1.
4. On the screen open the closed problem.

## Expected Result
Step 1: refused. Step 2: refused. Step 3: 404. Step 4: read-only with "This problem is closed (since …) and cannot be changed."

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/servicestatus/service/IncidentAndProblemTest.java` — `aClosedProblemTakesNoMoreIncidentsAndAProblemMustHaveATitle`
- `frontend/src/pages/admin/AdminProblemsPage.test.tsx` — "shows a closed problem read-only"

## Actual Result
The automated tests above passed on 2026-10-14.

## Status
Passed (automated run 2026-10-14)

## Linked Defect (if failed)
None.
