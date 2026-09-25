# TC-IAM-019: Role and Permission Administration — AC-7

| Field | Value |
|---|---|
| Test Case ID (required) | TC-IAM-019 |
| Requirement ID (required) | [REQ-IAM-003](../../../docs/02-requirements/FRD/role-permission-administration/requirement.md) |
| Acceptance Criterion | [AC-7](../../../docs/02-requirements/FRD/role-permission-administration/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-IAM-003](TESTPLAN-IAM-003.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A permission granted by at least one role.

## Steps
1. Arrange: a permission granted by at least one role.
2. Act: delete is attempted.
3. Observe the response and the UI.

## Expected Result
The response is 400 naming those roles.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/authorization/service/RoleAdminServiceTest.java` — "aPermissionStillGrantedByARoleCannotBeDeleted"
- `frontend/src/pages/admin/PermissionsAdminPage.test.tsx` — "shows the backend refusal to delete a permission still granted by a role"

## Actual Result
All automated tests below passed on 2026-09-25.

## Status
Passed (automated run 2026-09-25)

## Linked Defect (if failed)
None.
