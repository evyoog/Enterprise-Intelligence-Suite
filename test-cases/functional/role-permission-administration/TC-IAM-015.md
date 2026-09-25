# TC-IAM-015: Role and Permission Administration — AC-3

| Field | Value |
|---|---|
| Test Case ID (required) | TC-IAM-015 |
| Requirement ID (required) | [REQ-IAM-003](../../../docs/02-requirements/FRD/role-permission-administration/requirement.md) |
| Acceptance Criterion | [AC-3](../../../docs/02-requirements/FRD/role-permission-administration/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-IAM-003](TESTPLAN-IAM-003.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Partly |

## Preconditions
An existing role name.

## Steps
1. Arrange: an existing role name.
2. Act: a role with the same name is created.
3. Observe the response and the UI.

## Expected Result
The response is 409 and the UI shows the backend message.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/authorization/service/RoleAdminServiceTest.java` — "cannotCreateADuplicateRoleName"

**Manual check:** Check the UI shows the 409 message.

## Actual Result
The automated tests below passed on 2026-09-25; the manual check is not yet run.

## Status
Not Run

## Linked Defect (if failed)
None.
