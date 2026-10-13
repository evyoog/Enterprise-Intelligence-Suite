# TC-TEN-071: Access management — Guard rails

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-071 |
| Requirement ID (required) | [REQ-TEN-005](../../../docs/02-requirements/FRD/access-management/requirement.md) (REQ-TEN-005.6, BR-2, BR-3, BR-4, BR-6, AM-10) |
| Acceptance Criterion | [AC-11, AC-12, AC-13](../../../docs/02-requirements/FRD/access-management/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-005](TESTPLAN-TEN-005.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes (`AccessManagementServiceTest.theGuardRailsRefuse...`, `roleChangesFollowTheSameGuardRailsAndRecalculateAccessKeepingOverrides`; UI: "read-only panel") |

## Steps
1. As an admin and as a delegate, change your own access or role.
2. As a delegate, change an admin's access or role, or make someone an admin.
3. As an admin, turn an admin's permission OFF.
4. As a delegate without *Approve orders*, grant it; grant *Manage access* (which they hold).
5. As an ordinary member open the Access management API; as an admin ask for a member of another organization.

## Expected Result
Each refusal is a 403 naming the rule ("own access", "administrator", "locked", "do not hold"); granting a permission the delegate holds works; an ordinary member gets 403 (Manage members does not include Manage access); another organization's member is 404. The role change keeps the member's overrides and recalculates from the new role.

## Status
Passed (automated, 2026-10-12).
