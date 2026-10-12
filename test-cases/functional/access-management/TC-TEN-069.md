# TC-TEN-069: Access management — Delegate Manage subscriptions

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-069 |
| Requirement ID (required) | [REQ-TEN-005](../../../docs/02-requirements/FRD/access-management/requirement.md) (REQ-TEN-005.4, .3, BR-11 (D16)) |
| Acceptance Criterion | [AC-5, AC-7, AC-8](../../../docs/02-requirements/FRD/access-management/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-005](TESTPLAN-TEN-005.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes (`AccessManagementServiceTest.anAdminDelegatesManageSubscriptionsAndTheMemberCanThenActOnTheSubscription`) |

## Steps
1. As a member without the permission, suspend the organization subscription.
2. As an admin, turn *Manage subscriptions* ON for the member.
3. As the member, suspend and reactivate the subscription.
4. Turn the permission OFF, then try to cancel and to change the plan.

## Expected Result
Step 1 is refused (403). After the grant the member can suspend and reactivate. After the removal, cancel and change plan are refused at once.

## Status
Passed (automated, 2026-10-12).
