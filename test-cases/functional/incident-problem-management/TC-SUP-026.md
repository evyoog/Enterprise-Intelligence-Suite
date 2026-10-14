# TC-SUP-026: A problem links several incidents; an incident is in one problem only

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SUP-026 |
| Requirement ID (required) | [REQ-SUP-003](../../../docs/02-requirements/FRD/incident-problem-management/requirement.md) (C101) |
| Acceptance Criterion | [AC-6](../../../docs/02-requirements/FRD/incident-problem-management/acceptance-criteria.md) |
| Business rule(s) | [BR-INC-010, BR-INC-011](../../../docs/02-requirements/FRD/incident-problem-management/business-rules.md) |
| Decision | [C101](../../../docs/01-business/roadmap/open-decisions.md#c101) |
| Test Plan | [TESTPLAN-SUP-003](TESTPLAN-SUP-003.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
Two incidents A and B; two problems P and Q.

## Steps
1. Create P with A and B.
2. Link A to Q.
3. Unlink B from P, then link B to Q.
4. On the screen create a problem with picked incidents and link and remove an incident.

## Expected Result
Step 1: both are linked and show P's title. Step 2: refused ("already belongs"). Step 3: B is free and then belongs to Q. Step 4: only incidents not yet in a problem are offered.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/servicestatus/service/IncidentAndProblemTest.java` — `aProblemLinksSeveralIncidentsAndAnIncidentBelongsToOnlyOne`
- `frontend/src/pages/admin/AdminProblemsPage.test.tsx` — "creates a problem and links the incidents picked (only those not already in a problem are listed)", "links an incident that is not in a problem yet and removes a linked one"

## Actual Result
The automated tests above passed on 2026-10-14.

## Status
Passed (automated run 2026-10-14)

## Linked Defect (if failed)
None.
