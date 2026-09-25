# TC-IAM-020: Role and Permission Administration — AC-8

| Field | Value |
|---|---|
| Test Case ID (required) | TC-IAM-020 |
| Requirement ID (required) | [REQ-IAM-003](../../../docs/02-requirements/FRD/role-permission-administration/requirement.md) |
| Acceptance Criterion | [AC-8](../../../docs/02-requirements/FRD/role-permission-administration/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-IAM-003](TESTPLAN-IAM-003.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A system-managed permission.

## Steps
1. Arrange: a system-managed permission.
2. Act: delete is attempted.
3. Observe the response and the UI.

## Expected Result
The response is 403.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/authorization/service/RoleAdminServiceTest.java` — "protectedPermissionsCannotBeDeleted"

## Actual Result
All automated tests below passed on 2026-09-25.

## Status
Passed (automated run 2026-09-25)

## Linked Defect (if failed)
None.
