# TC-TEN-063: Projects — members get the products and lose them again

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-063 |
| Requirement ID (required) | [REQ-TEN-009](../../../docs/02-requirements/FRD/projects/requirement.md) (REQ-TEN-009.4, REQ-TEN-009.7, REQ-TEN-009.8, BR-PRJ-004, BR-PRJ-005, BR-PRJ-008) |
| Acceptance Criterion | [AC-4, AC-7](../../../docs/02-requirements/FRD/projects/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-009](TESTPLAN-TEN-009.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes (`ProjectServiceTest.projectMembersGetTheProjectsProductsAndLoseThemAgain`) |

## Steps
1. Add a member and a product with a role to a project.
2. Add a second member and a second product, and change the first product's role.
3. Remove a product, then a member, then delete the project.

## Expected Result
Each member holds access with the project's role, recorded as coming from the project. The member is notified once per new product. Removing a product, a member or the project makes only the project's access INACTIVE (source cleared) and keeps the rest.

## Status
Passed (automated, 2026-10-12).
