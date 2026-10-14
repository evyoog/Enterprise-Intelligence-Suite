# TC-SUB-041: The scheduled plan applies when the subscription renews automatically

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SUB-041 |
| Requirement ID (required) | [REQ-SUB-001](../../../docs/02-requirements/FRD/subscription-lifecycle/requirement.md) (.9; BR-SUB-012) |
| Acceptance Criterion | [AC-16](../../../docs/02-requirements/FRD/subscription-lifecycle/acceptance-criteria.md) |
| Decision | [C97](../../../docs/01-business/roadmap/open-decisions.md#c97) |
| Test Plan | [TESTPLAN-SUB-001](TESTPLAN-SUB-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A subscription on Pro with a downgrade to Basic scheduled, whose renewal date has passed.

## Steps
1. Run the renewal job (`RenewalService.renewDue`).
2. Read the subscription.

## Expected Result
The plan is Basic, nothing is pending, and the term moved on at the new plan.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/registration/service/ScheduledPlanChangeTest.java` — `aCheaperPlanIsScheduledAndTakesEffectWhenTheSubscriptionRenews` (renewal part)

## Actual Result
The automated tests below passed on 2026-10-13 (`mvn -B -o test -Dtest=...`).

## Status
Passed (automated run 2026-10-13)

## Linked Defect (if failed)
None.
