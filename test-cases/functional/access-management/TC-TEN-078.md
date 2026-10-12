# TC-TEN-078: Access management — Members list: filters, paging and catalogue

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-078 |
| Requirement ID (required) | [REQ-TEN-005](../../../docs/02-requirements/FRD/access-management/requirement.md) (REQ-TEN-005.7, UI) |
| Acceptance Criterion | [AC-15, AC-21](../../../docs/02-requirements/FRD/access-management/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-005](TESTPLAN-TEN-005.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes (`AccessManagementServiceTest.theMemberListFiltersAndPagesAndTheCatalogueShowsWhatTheCallerMayGrant`; UI: "lists members", "filters members") |

## Steps
1. List members with page size 2, by role, by custom access, by product, by a search that matches nobody.
2. Read the permission catalogue as an admin.

## Expected Result
Paging, role, custom, product and search filters work and the total is correct; the catalogue lists 9 permissions, each marked grantable.

## Status
Passed (automated, 2026-10-12).
