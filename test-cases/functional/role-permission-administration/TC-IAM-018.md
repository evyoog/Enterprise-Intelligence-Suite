# TC-IAM-018: Role and Permission Administration — AC-6

| Field | Value |
|---|---|
| Test Case ID (required) | TC-IAM-018 |
| Requirement ID (required) | [REQ-IAM-003](../../../docs/02-requirements/FRD/role-permission-administration/requirement.md) |
| Acceptance Criterion | [AC-6](../../../docs/02-requirements/FRD/role-permission-administration/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-IAM-003](TESTPLAN-IAM-003.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Partly |

## Preconditions
A platform administrator.

## Steps
1. Arrange: a platform administrator.
2. Act: they create a permission with a new name.
3. Observe the response and the UI.

## Expected Result
It appears in the permissions list with `roleCount` 0.

## Automated coverage
- `frontend/src/pages/admin/PermissionsAdminPage.test.tsx` — "creates a permission"

**Manual check:** Check the new permission is listed with `roleCount` 0.

## Actual Result
The automated tests below passed on 2026-09-25; the manual check is not yet run.

## Status
Not Run

## Linked Defect (if failed)
None.
