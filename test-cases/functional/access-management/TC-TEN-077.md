# TC-TEN-077: Access management — Manage billing is its own permission

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-077 |
| Requirement ID (required) | [REQ-TEN-005](../../../docs/02-requirements/FRD/access-management/requirement.md) (REQ-TEN-005 catalogue, AM-2, BR-ACC-001) |
| Acceptance Criterion | [AC-18](../../../docs/02-requirements/FRD/access-management/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-005](TESTPLAN-TEN-005.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes (`AccessManagementServiceTest.manageBillingIsItsOwnPermissionAndManageOrganizationNoLongerOpensBilling`) |

## Steps
1. Give a member *Manage organization* only; try the organization billing endpoints.
2. Also give *Manage billing*.

## Expected Result
With only *Manage organization* the member can use organization settings but billing is refused (403). With *Manage billing* the billing endpoints work. Admins hold both.

## Status
Passed (automated, 2026-10-12).
