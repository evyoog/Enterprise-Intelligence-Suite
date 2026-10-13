# TC-TEN-060: Projects — create, rename, delete and unique names

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-060 |
| Requirement ID (required) | [REQ-TEN-009](../../../docs/02-requirements/FRD/projects/requirement.md) (REQ-TEN-009.1, BR-PRJ-001) |
| Acceptance Criterion | [AC-1](../../../docs/02-requirements/FRD/projects/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-009](TESTPLAN-TEN-009.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes (`ProjectServiceTest.anAdministratorCreatesRenamesAndDeletesProjectsWithUniqueNames`) |

## Steps
1. As an organization administrator create the project "Mumbai rollout" with a description.
2. Create "MUMBAI ROLLOUT", and a project with an empty name or a 501-character description.
3. Create "Delhi", rename it to the first project's name, then to "Delhi 2".
4. List the projects and delete "Delhi 2".

## Expected Result
The first project is created with the trimmed name. The duplicate name (ignoring case), the empty name and the long description are refused. The list is sorted by name and the deleted project is gone.

## Status
Passed (automated, 2026-10-12).
