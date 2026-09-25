# TC-IAM-016: Role and Permission Administration — AC-4

| Field | Value |
|---|---|
| Test Case ID (required) | TC-IAM-016 |
| Requirement ID (required) | [REQ-IAM-003](../../../docs/02-requirements/FRD/role-permission-administration/requirement.md) |
| Acceptance Criterion | [AC-4](../../../docs/02-requirements/FRD/role-permission-administration/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-IAM-003](TESTPLAN-IAM-003.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A role.

## Steps
1. Arrange: a role.
2. Act: its description or permissions are edited.
3. Observe the response and the UI.

## Expected Result
The change is saved, and its name and scope stay unchanged.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/authorization/service/RoleAdminServiceTest.java` — "aNewlyCreatedRoleCanBeUpdatedAndThenDeleted"
- `frontend/src/pages/admin/RolesAdminPage.test.tsx` — "edits only the description and permissions; name and scope are read-only"

## Actual Result
All automated tests below passed on 2026-09-25.

## Status
Passed (automated run 2026-09-25)

## Linked Defect (if failed)
None.
