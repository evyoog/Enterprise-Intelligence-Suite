# TC-TEN-070: Access management — Individual product override and reset

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-070 |
| Requirement ID (required) | [REQ-TEN-005](../../../docs/02-requirements/FRD/access-management/requirement.md) (REQ-TEN-005.3, BR-1, BR-10) |
| Acceptance Criterion | [AC-5, AC-6](../../../docs/02-requirements/FRD/access-management/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-005](TESTPLAN-TEN-005.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes (`AccessManagementServiceTest.aProductIsTurnedOffAndOnIndividuallyAndResetReturnsToTheRoleDefault`; UI: `AccessManagement.test.tsx` "stages a reset") |

## Steps
1. Turn a product OFF for a member; recalculate access.
2. Reset the product to the role default.
3. Turn it off and *Approve orders* on, then Reset all.

## Expected Result
The product is off and stays off after a recalculation (source "Custom"). Reset returns it to "From role" and ON. Reset all removes every override.

## Status
Passed (automated, 2026-10-12).
