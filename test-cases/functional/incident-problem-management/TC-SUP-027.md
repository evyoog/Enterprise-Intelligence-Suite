# TC-SUP-027: Problem statuses: root cause before Resolved, every action done before Closed

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SUP-027 |
| Requirement ID (required) | [REQ-SUP-003](../../../docs/02-requirements/FRD/incident-problem-management/requirement.md) (C101) |
| Acceptance Criterion | [AC-7](../../../docs/02-requirements/FRD/incident-problem-management/acceptance-criteria.md) |
| Business rule(s) | [BR-INC-012, BR-INC-013, BR-INC-014](../../../docs/02-requirements/FRD/incident-problem-management/business-rules.md) |
| Decision | [C101](../../../docs/01-business/roadmap/open-decisions.md#c101) |
| Test Plan | [TESTPLAN-SUP-003](TESTPLAN-SUP-003.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A new problem.

## Steps
1. Resolve it with no root cause; close it.
2. Write the root cause and set ROOT_CAUSE_IDENTIFIED.
3. Add the action "Turn log rotation on"; resolve the problem.
4. Close it while the action is open; mark the action done; close it.
5. Change its title; add an action.
6. On the screen, try the blocked moves and add, edit and delete an action.

## Expected Result
Step 1: both refused ("root cause"). Step 2: ROOT_CAUSE_IDENTIFIED. Step 3: RESOLVED with a resolved time, 1 open action. Step 4: refused ("corrective action") until the action is done (done time set), then CLOSED with a closing time. Step 5: both refused. Step 6: the screen explains why a move is blocked and deletes an action only after confirmation.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/servicestatus/service/IncidentAndProblemTest.java` — `aProblemNeedsARootCauseToBeResolvedAndEveryActionDoneToBeClosed`
- `frontend/src/pages/admin/AdminProblemsPage.test.tsx` — "blocks moving on without a root cause, explains why, and allows it once the root cause is written", "blocks closing a resolved problem while a corrective action is open, then allows it once every action is done", "adds, edits and deletes corrective actions, deleting only after confirmation", "saves the details and shows what the server refuses"

## Actual Result
The automated tests above passed on 2026-10-14.

## Status
Passed (automated run 2026-10-14)

## Linked Defect (if failed)
None.
