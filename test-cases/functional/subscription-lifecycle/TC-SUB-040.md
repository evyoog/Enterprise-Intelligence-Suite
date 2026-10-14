# TC-SUB-040: A scheduled downgrade can be withdrawn

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SUB-040 |
| Requirement ID (required) | [REQ-SUB-001](../../../docs/02-requirements/FRD/subscription-lifecycle/requirement.md) (.10; BR-SUB-013) |
| Acceptance Criterion | [AC-15](../../../docs/02-requirements/FRD/subscription-lifecycle/acceptance-criteria.md) |
| Decision | [C97](../../../docs/01-business/roadmap/open-decisions.md#c97) |
| Test Plan | [TESTPLAN-SUB-001](TESTPLAN-SUB-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A subscription on Pro with a downgrade to Basic scheduled.

## Steps
1. Withdraw the scheduled change (`DELETE /me/subscriptions/{id}/plan/scheduled`).

## Expected Result
`pendingPlanId` is null and the plan is still Pro. (`SUBSCRIPTION_PLAN_CHANGE_WITHDRAWN` is audited.)

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/registration/service/ScheduledPlanChangeTest.java` — `aCheaperPlanIsScheduledAndTakesEffectWhenTheSubscriptionRenews` (withdraw part)

Not covered by an automated test: the audit entry, withdrawing when nothing is pending, and the organization endpoint `DELETE /organization/me/subscriptions/{id}/plan/scheduled`.

## Actual Result
The automated tests below passed on 2026-10-13 (`mvn -B -o test -Dtest=...`).

## Status
Passed (automated run 2026-10-13)

## Linked Defect (if failed)
None.
