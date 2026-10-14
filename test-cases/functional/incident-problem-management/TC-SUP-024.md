# TC-SUP-024: A resolved incident is reopened by removing its end time

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SUP-024 |
| Requirement ID (required) | [REQ-SUP-003](../../../docs/02-requirements/FRD/incident-problem-management/requirement.md) (C101) |
| Acceptance Criterion | [AC-4](../../../docs/02-requirements/FRD/incident-problem-management/acceptance-criteria.md) |
| Business rule(s) | [BR-INC-005](../../../docs/02-requirements/FRD/incident-problem-management/business-rules.md) |
| Decision | [C101](../../../docs/01-business/roadmap/open-decisions.md#c101) |
| Test Plan | [TESTPLAN-SUP-003](TESTPLAN-SUP-003.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An incident resolved through an end time and a summary.

## Steps
1. Update it with no end time and no phase.
2. On the screen, open a resolved incident and choose "(reopen)".

## Expected Result
The incident is INVESTIGATING and open; the screen offers reopening and sends no end time.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/servicestatus/service/IncidentAndProblemTest.java` — `aResolvedIncidentCanBeReopenedByRemovingTheEndTime`
- `frontend/src/pages/admin/AdminIncidentsPage.test.tsx` — "offers reopening a resolved incident and sends no end time"

## Actual Result
The automated tests above passed on 2026-10-14.

## Status
Passed (automated run 2026-10-14)

## Linked Defect (if failed)
None.
