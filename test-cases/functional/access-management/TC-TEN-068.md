# TC-TEN-068: Access management — Default access from role

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-068 |
| Requirement ID (required) | [REQ-TEN-005](../../../docs/02-requirements/FRD/access-management/requirement.md) (REQ-TEN-005.1, .2, BR-1, BR-15 (AM-1, AM-7)) |
| Acceptance Criterion | [AC-1, AC-4](../../../docs/02-requirements/FRD/access-management/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-005](TESTPLAN-TEN-005.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes (`AccessManagementServiceTest.anEntitledProductIsOnForMembersByDefaultWithTheDefaultRoleAndAdminsAreLockedOn`) |

## Steps
1. Subscribe an organization to a product and add an organization admin and a member.
2. Read the member's access and permissions, and the admin's.

## Expected Result
The member has the product ON with the role `USER`, source "From role", and no feature permissions. The admin holds every feature permission, shown locked ON, and cannot be changed by themselves.

## Status
Passed (automated, 2026-10-12).
