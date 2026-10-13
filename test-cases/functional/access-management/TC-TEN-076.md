# TC-TEN-076: Access management — Subscription changes recalculate access

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-076 |
| Requirement ID (required) | [REQ-TEN-005](../../../docs/02-requirements/FRD/access-management/requirement.md) (REQ-TEN-005.5, BR-7) |
| Acceptance Criterion | [AC-9](../../../docs/02-requirements/FRD/access-management/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-005](TESTPLAN-TEN-005.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes (`AccessManagementServiceTest.anEndedSubscriptionRemovesTheProductFromEffectiveAccessAndReactivatingBringsItBack`) |

## Steps
1. Suspend the organization's subscription as an admin.
2. Reactivate it; then cancel it.
3. Try to set a role default for the cancelled product.

## Expected Result
The product disappears from members' access and from the screen while suspended and returns on reactivation; after cancellation it stays off and role defaults for it are refused (400).

## Status
Passed (automated, 2026-10-12).
