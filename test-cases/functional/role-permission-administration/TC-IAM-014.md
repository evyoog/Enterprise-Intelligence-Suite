# TC-IAM-014: Role and Permission Administration — AC-2

| Field | Value |
|---|---|
| Test Case ID (required) | TC-IAM-014 |
| Requirement ID (required) | [REQ-IAM-003](../../../docs/02-requirements/FRD/role-permission-administration/requirement.md) |
| Acceptance Criterion | [AC-2](../../../docs/02-requirements/FRD/role-permission-administration/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-IAM-003](TESTPLAN-IAM-003.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A platform administrator.

## Steps
1. Arrange: a platform administrator.
2. Act: they create an ORGANIZATION role named anything other than `ORG_ADMIN` or `MEMBER`.
3. Observe the response and the UI.

## Expected Result
The response is 400 and the UI shows the backend message.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/authorization/service/RoleAdminServiceTest.java` — "cannotCreateAnOrganizationScopeRoleWithANameOutsideTheOrgRoleEnum"
- `frontend/src/pages/admin/RolesAdminPage.test.tsx` — "shows the backend message when an organization-scope name is refused"

## Actual Result
All automated tests below passed on 2026-09-25.

## Status
Passed (automated run 2026-09-25)

## Linked Defect (if failed)
None.
