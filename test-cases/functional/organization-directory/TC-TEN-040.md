# TC-TEN-040: Organizations directory — filters

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-040 |
| Requirement ID (required) | [REQ-TEN-007](../../../docs/02-requirements/FRD/organization-directory/requirement.md) |
| Acceptance Criterion | [AC-2](../../../docs/02-requirements/FRD/organization-directory/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-007](TESTPLAN-TEN-007.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Steps
1. Filter by type, profile, seats, lifecycle and search; then Clear filters.

## Expected Result
Each filter narrows the list and the shown count; Clear filters restores it.

## Automated coverage
- `frontend/src/pages/admin/AdminOrganizationsPage.test.tsx` — `narrows by type, by profile completion and by search…`, `filters by seat usage and lifecycle`

## Actual Result
The automated tests passed on 2026-10-08.

## Status
Passed
