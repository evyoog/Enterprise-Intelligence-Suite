# TC-TEN-064: Projects — direct access wins over a project

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-064 |
| Requirement ID (required) | [REQ-TEN-009](../../../docs/02-requirements/FRD/projects/requirement.md) (REQ-TEN-009.5, BR-PRJ-004, BR-PRJ-006) |
| Acceptance Criterion | [AC-5](../../../docs/02-requirements/FRD/projects/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-009](TESTPLAN-TEN-009.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes (`ProjectServiceTest.aDirectAssignmentWinsOverAProjectAndAProjectGrantCannotBeRevokedDirectly`) |

## Steps
1. Give a member direct access to a product, then put the member and the product in a project.
2. Try to revoke a project member's access directly.
3. Assign access directly to that member, then delete the project.

## Expected Result
The direct row keeps its role and no source. The direct revoke of project access is refused (409) with the project's name. A direct assignment over project access makes it direct, so it survives the project's deletion and can be revoked directly.

## Status
Passed (automated, 2026-10-12).
