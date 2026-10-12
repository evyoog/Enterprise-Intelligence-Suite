# TC-TEN-072: Access management — Role defaults with preview

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-072 |
| Requirement ID (required) | [REQ-TEN-005](../../../docs/02-requirements/FRD/access-management/requirement.md) (REQ-TEN-005.2, BR-1, BR-4, BR-6, BR-13, AM-9) |
| Acceptance Criterion | [AC-3, AC-4](../../../docs/02-requirements/FRD/access-management/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-005](TESTPLAN-TEN-005.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes (`AccessManagementServiceTest.roleDefaultsAreChangedWithAPreviewAndOnlyAffectMembersWithoutAnOverride`; UI: "edits role defaults") |

## Steps
1. Turn *Approve orders* ON and a product OFF for the Member role; preview, then save.
2. Add a new member.
3. Try to turn a permission OFF for Organization admin; as a delegate edit the Organization admin defaults.

## Expected Result
The preview changes nothing and shows the impact per item. Saving applies to members without an override (members with an individual override keep it) and writes one audit entry per item with the number of members affected. A new member follows the organization's defaults. The admin role stays locked ON and only admins edit its defaults.

## Status
Passed (automated, 2026-10-12).
