# TC-TEN-065: Projects — a member in two projects

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-065 |
| Requirement ID (required) | [REQ-TEN-009](../../../docs/02-requirements/FRD/projects/requirement.md) (REQ-TEN-009.6, BR-PRJ-005) |
| Acceptance Criterion | [AC-6](../../../docs/02-requirements/FRD/projects/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-009](TESTPLAN-TEN-009.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes (`ProjectServiceTest.aMemberInTwoProjectsKeepsTheProductWhileOneSourceRemains`) |

## Steps
1. Put the same product in two projects with different roles and add the member to both.
2. Remove the member from the first project.
3. Remove the product from the second project.

## Expected Result
One access row exists and the member is notified once. After step 2 access stays, now from the second project with its role. After step 3 it is INACTIVE.

## Status
Passed (automated, 2026-10-12).
