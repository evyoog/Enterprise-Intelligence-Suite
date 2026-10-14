# TC-SUB-039: An upgrade, or a same-price plan, is immediate and clears a scheduled downgrade

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SUB-039 |
| Requirement ID (required) | [REQ-SUB-001](../../../docs/02-requirements/FRD/subscription-lifecycle/requirement.md) (.5, .10; BR-SUB-007) |
| Acceptance Criterion | [AC-14](../../../docs/02-requirements/FRD/subscription-lifecycle/acceptance-criteria.md) |
| Decision | [C97](../../../docs/01-business/roadmap/open-decisions.md#c97) |
| Test Plan | [TESTPLAN-SUB-001](TESTPLAN-SUB-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
Subscription on Pro with a downgrade to Basic scheduled; a more expensive plan Enterprise (900); two plans of the same price (0) on another product.

## Steps
1. Change the plan to Enterprise.
2. On the other subscription, change between two plans of equal price.

## Expected Result
Step 1: the plan is Enterprise at once and nothing is pending. Step 2: the plan changes at once, not scheduled.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/registration/service/ScheduledPlanChangeTest.java` — `aCheaperPlanIsScheduledAndTakesEffectWhenTheSubscriptionRenews` (upgrade part)
- `backend/src/test/java/com/vyoog/eisplatform/modules/registration/service/ScheduledPlanChangeTest.java` — `anUpgradeIsImmediateAndADowngradeWaitsForTheRenewalAndCanBeWithdrawn` (same-price plan)

## Actual Result
The automated tests below passed on 2026-10-13 (`mvn -B -o test -Dtest=...`).

## Status
Passed (automated run 2026-10-13)

## Linked Defect (if failed)
None.
