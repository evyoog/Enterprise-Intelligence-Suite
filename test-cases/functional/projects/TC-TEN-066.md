# TC-TEN-066: Projects — permissions, organization scope and plain groups

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-066 |
| Requirement ID (required) | [REQ-TEN-009](../../../docs/02-requirements/FRD/projects/requirement.md) (REQ-TEN-009.9, BR-PRJ-007) |
| Acceptance Criterion | [AC-8, AC-9](../../../docs/02-requirements/FRD/projects/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-009](TESTPLAN-TEN-009.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes (`ProjectServiceTest.permissionsAndOrganizationScopeAreEnforcedAndGroupsStayApart`) |

## Steps
1. As a plain member try to list, create, add a member and add a product.
2. As an administrator of another organization try to rename and delete the project, and list projects.
3. List groups and use the group endpoints on the project.

## Expected Result
The plain member is refused (403). The other organization's administrator gets "not found" and an empty list. Groups lists no project, and adding a member to or deleting the project through the Groups endpoints is "not found".

## Status
Passed (automated, 2026-10-12).
