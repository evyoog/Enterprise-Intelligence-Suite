# TC-SUP-022: Incident phases: Resolved needs a summary, Closed only after Resolved and is final

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SUP-022 |
| Requirement ID (required) | [REQ-SUP-003](../../../docs/02-requirements/FRD/incident-problem-management/requirement.md) (C101) |
| Acceptance Criterion | [AC-2](../../../docs/02-requirements/FRD/incident-problem-management/acceptance-criteria.md) |
| Business rule(s) | [BR-INC-004, BR-INC-006](../../../docs/02-requirements/FRD/incident-problem-management/business-rules.md) |
| Decision | [C101](../../../docs/01-business/roadmap/open-decisions.md#c101) |
| Test Plan | [TESTPLAN-SUP-003](TESTPLAN-SUP-003.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A LOGGED incident.

## Steps
1. Move it to INVESTIGATING.
2. Mark it RESOLVED with a blank summary.
3. Mark it CLOSED.
4. Mark it RESOLVED with the summary "Restarted the queue worker".
5. Mark it CLOSED; then try to change it.

## Expected Result
Step 1: INVESTIGATING. Step 2: refused ("resolution summary"). Step 3: refused ("resolved"). Step 4: RESOLVED, end time set, no longer open. Step 5: CLOSED with a closing time; the change is refused ("closed").

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/servicestatus/service/IncidentAndProblemTest.java` — `theIncidentMovesThroughItsPhasesAndResolvedNeedsASummary`

## Actual Result
The automated tests above passed on 2026-10-14.

## Status
Passed (automated run 2026-10-14)

## Linked Defect (if failed)
None.
