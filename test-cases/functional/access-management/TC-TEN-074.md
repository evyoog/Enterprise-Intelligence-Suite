# TC-TEN-074: Access management — Bulk change

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-074 |
| Requirement ID (required) | [REQ-TEN-005](../../../docs/02-requirements/FRD/access-management/requirement.md) (REQ-TEN-005.8, BR-12) |
| Acceptance Criterion | [AC-16](../../../docs/02-requirements/FRD/access-management/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-005](TESTPLAN-TEN-005.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes (`AccessManagementServiceTest.bulkChangesApplyMemberByMemberAndReportWhySomeWereSkipped`; UI: "previews and applies a bulk change") |

## Steps
1. Select four members, one of them the caller, and preview granting a product with two free seats.
2. Apply; then grant and remove a permission for two members (one id repeated).
3. Send an unknown item.

## Expected Result
The preview changes nothing and says how many would change and who is skipped, with reasons ("No free seats", "own access"). Apply changes only those members. An unknown item is a 400.

## Status
Passed (automated, 2026-10-12).
