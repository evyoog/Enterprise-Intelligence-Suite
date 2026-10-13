# TC-TEN-075: Access management — Audit, Activity and notices

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-075 |
| Requirement ID (required) | [REQ-TEN-005](../../../docs/02-requirements/FRD/access-management/requirement.md) (REQ-TEN-005.9, .12, BR-13, BR-14 (AM-6)) |
| Acceptance Criterion | [AC-17](../../../docs/02-requirements/FRD/access-management/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-005](TESTPLAN-TEN-005.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes (`AccessManagementServiceTest.everyChangeIsAuditedAndTheMemberIsToldInTheApp`; UI: "Activity tab") |

## Steps
1. As an admin change a member's permission and product in one request.
2. Read the member's notifications and the Activity list; reset an item.
3. As an ordinary member open Activity.

## Expected Result
The member gets one in-app notice (and an email unless muted) naming everything that changed. Activity shows who, what and from → to with the basis. The reset is recorded. An ordinary member is refused.

## Status
Passed (automated, 2026-10-12).
