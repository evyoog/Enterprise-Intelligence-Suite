# TC-IAM-013: Role and Permission Administration — AC-1

| Field | Value |
|---|---|
| Test Case ID (required) | TC-IAM-013 |
| Requirement ID (required) | [REQ-IAM-003](../../../docs/02-requirements/FRD/role-permission-administration/requirement.md) |
| Acceptance Criterion | [AC-1](../../../docs/02-requirements/FRD/role-permission-administration/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-IAM-003](TESTPLAN-IAM-003.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Partly |

## Preconditions
A platform administrator.

## Steps
1. Arrange: a platform administrator.
2. Act: they create a PLATFORM role with a new name and some permissions.
3. Observe the response and the UI.

## Expected Result
It appears in the roles list with those permissions and a `ROLE_CREATED` audit record exists.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/authorization/service/RoleAdminServiceTest.java` — "canCreateAPlatformScopeRoleWithAnyName"
- `frontend/src/pages/admin/RolesAdminPage.test.tsx` — "creates a role with the selected permissions"

**Manual check:** Check a `ROLE_CREATED` audit record exists.

## Actual Result
The automated tests below passed on 2026-09-25; the manual check is not yet run.

## Status
Not Run

## Linked Defect (if failed)
None.
