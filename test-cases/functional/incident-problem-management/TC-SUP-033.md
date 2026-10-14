# TC-SUP-033: The Problems tab: list, create, detail, blocked moves, actions, closed read-only

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SUP-033 |
| Requirement ID (required) | [REQ-SUP-003](../../../docs/02-requirements/FRD/incident-problem-management/requirement.md) (C101) |
| Acceptance Criterion | [AC-13](../../../docs/02-requirements/FRD/incident-problem-management/acceptance-criteria.md) |
| Business rule(s) | [BR-INC-010 to BR-INC-015](../../../docs/02-requirements/FRD/incident-problem-management/business-rules.md) |
| Decision | [C101](../../../docs/01-business/roadmap/open-decisions.md#c101) |
| Test Plan | [TESTPLAN-SUP-003](TESTPLAN-SUP-003.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A platform administrator on Platform → Service status → Problems.

## Steps
1. Open the tab; with no problems; when the API fails.
2. Create a problem with incidents.
3. Open a problem: move its status, add, edit and delete actions, link and remove an incident, save the details.
4. Open a closed problem.

## Expected Result
The list shows status, incidents and open/total actions with no axe violations; the empty state and a retry; the problem is created with the incidents picked; a blocked move is explained and allowed once its condition holds; actions are added, edited and deleted after confirmation; a closed problem is read-only.

## Automated coverage
- `frontend/src/pages/admin/AdminProblemsPage.test.tsx` — "lists problems with status, incidents and open/total actions, and has no accessibility violations", "offers a retry when the list cannot be loaded and an empty state when there are no problems", "creates a problem and links the incidents picked (only those not already in a problem are listed)", "blocks moving on without a root cause, explains why, and allows it once the root cause is written", "blocks closing a resolved problem while a corrective action is open, then allows it once every action is done", "shows a closed problem read-only", "links an incident that is not in a problem yet and removes a linked one", "adds, edits and deletes corrective actions, deleting only after confirmation", "saves the details and shows what the server refuses"

## Actual Result
The automated tests above passed on 2026-10-14.

## Status
Passed (automated run 2026-10-14)

## Linked Defect (if failed)
None.
