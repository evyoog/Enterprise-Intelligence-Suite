# TC-SUB-038: A downgrade is scheduled, not applied

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SUB-038 |
| Requirement ID (required) | [REQ-SUB-001](../../../docs/02-requirements/FRD/subscription-lifecycle/requirement.md) (.5, .8; BR-SUB-011) |
| Acceptance Criterion | [AC-13](../../../docs/02-requirements/FRD/subscription-lifecycle/acceptance-criteria.md) |
| Decision | [C97](../../../docs/01-business/roadmap/open-decisions.md#c97) |
| Test Plan | [TESTPLAN-SUB-001](TESTPLAN-SUB-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An ACTIVE subscription on plan Pro (500 a month); a cheaper plan Basic (100 a month) of the same product.

## Steps
1. Change the plan to Basic.
2. Read the subscription.

## Expected Result
The plan is still Pro; `pendingPlanId` is Basic and `pendingPlanName` is "Basic". (`SUBSCRIPTION_PLAN_CHANGE_SCHEDULED` is audited and the owner notified.)

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/registration/service/ScheduledPlanChangeTest.java` — `aCheaperPlanIsScheduledAndTakesEffectWhenTheSubscriptionRenews` (scheduling part)

Not covered by an automated test: the audit entry and the notification; check them in the audit log and notifications.

## Actual Result
The automated tests below passed on 2026-10-13 (`mvn -B -o test -Dtest=...`).

## Status
Passed (automated run 2026-10-13)

## Linked Defect (if failed)
None.
