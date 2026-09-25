# TC-IAM-021: Role and Permission Administration — AC-9

| Field | Value |
|---|---|
| Test Case ID (required) | TC-IAM-021 |
| Requirement ID (required) | [REQ-IAM-003](../../../docs/02-requirements/FRD/role-permission-administration/requirement.md) |
| Acceptance Criterion | [AC-9](../../../docs/02-requirements/FRD/role-permission-administration/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-IAM-003](TESTPLAN-IAM-003.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A user without `MANAGE_ROLES`.

## Steps
1. Arrange: a user without `MANAGE_ROLES`.
2. Act: they call `/admin/roles`.
3. Observe the response and the UI.

## Expected Result
The response is 403.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/config/RbacAdminAuthorizationTest.java` — "nonAdminCannotListRoles"

## Actual Result
All automated tests below passed on 2026-09-25.

## Status
Passed (automated run 2026-09-25)

## Linked Defect (if failed)
None.
