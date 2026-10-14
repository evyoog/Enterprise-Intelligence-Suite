# TC-SUB-042: The scheduled plan applies when the customer renews manually

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SUB-042 |
| Requirement ID (required) | [REQ-SUB-001](../../../docs/02-requirements/FRD/subscription-lifecycle/requirement.md) (.9; BR-SUB-012) |
| Acceptance Criterion | [AC-17](../../../docs/02-requirements/FRD/subscription-lifecycle/acceptance-criteria.md) |
| Decision | [C97](../../../docs/01-business/roadmap/open-decisions.md#c97) |
| Test Plan | [TESTPLAN-SUB-001](TESTPLAN-SUB-001.md) |
| Priority | P1 |
| Type | Functional |
| Automated | No |

## Preconditions
A subscription on Pro with a downgrade to Basic scheduled.

## Steps
1. As the customer, renew the subscription (`POST /me/subscriptions/{id}/renew`).
2. Read the subscription.

## Expected Result
The plan is Basic, nothing is pending, the term follows the new plan's billing period and a renewal invoice is issued at the new plan.

## Automated coverage
None. `SubscriptionService#renewSubscription` calls `applyPendingPlan`, as the auto-renew path does. Add a test next to `ScheduledPlanChangeTest`.

## Actual Result
Not run. No automated test exists for the manual renewal path.

## Status
Not Run

## Linked Defect (if failed)
None.
