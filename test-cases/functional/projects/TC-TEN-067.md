# TC-TEN-067: Projects — Projects tab (screens)

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-067 |
| Requirement ID (required) | [REQ-TEN-009](../../../docs/02-requirements/FRD/projects/requirement.md) (REQ-TEN-009.10) |
| Acceptance Criterion | [AC-10](../../../docs/02-requirements/FRD/projects/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-009](TESTPLAN-TEN-009.md) |
| Priority | P0 |
| Type | Functional (UI) |
| Automated | Yes (`OrganizationProjectsCard.test.tsx`, `OrganizationMembersPage.test.tsx`) |

## Steps
1. Open People & structure as an administrator: the Projects tab is there. Open it as a member who holds only `MANAGE_PRODUCT_ACCESS`.
2. Create a project, rename it (a duplicate name shows the server message), add a member and a subscribed product with a role, remove them, delete the project.
3. Run the accessibility check on a project with members and products.

## Expected Result
The tab shows for `MANAGE_USERS` or `MANAGE_PRODUCT_ACCESS`. A product-access-only member sees people read-only and can manage products. Only products with an active subscription are offered. Text is translated (English and Spanish) and the page has no accessibility violations.

## Status
Passed (automated, 2026-10-12). Manual Spanish and keyboard check: not run.
