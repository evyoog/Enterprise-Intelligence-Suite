# TC-MKT-055: The owner is reminded once, 3 days before the trial ends

| Field | Value |
|---|---|
| Test Case ID (required) | TC-MKT-055 |
| Requirement ID (required) | [REQ-MKT-005](../../../docs/02-requirements/FRD/product-evaluation/requirement.md) (C102) |
| Acceptance Criterion | [AC-7](../../../docs/02-requirements/FRD/product-evaluation/acceptance-criteria.md) |
| Business rule(s) | [BR-EVL-010, BR-EVL-011](../../../docs/02-requirements/FRD/product-evaluation/business-rules.md) |
| Decision | [C102](../../../docs/01-business/roadmap/open-decisions.md#c102) |
| Test Plan | [TESTPLAN-MKT-005](TESTPLAN-MKT-005.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A running trial of an individual whose `expiresAt` is 2 days away.

## Steps
1. Run `SubscriptionService#remindEndingTrials` (the hourly job does) twice.
2. Read the customer's notifications.

## Expected Result
Exactly one notice "Your free trial of {product} ends soon" with the end date; `trial_reminded_at` is set; the second run sends nothing.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/evaluation/service/TrialDemoCompareTest.java` — `theOwnerIsRemindedOnceThreeDaysBeforeTheTrialEnds`

The notice to an organization-owned trial is not sent (BR-EVL-010); not tested.

## Actual Result
The backend tests (`TrialDemoCompareTest`, 12 tests, and `EvaluationAuthorizationTest`, 2 tests) passed on 2026-10-15; the full backend run of the same day had 876 tests, 0 failures and 28 skipped.

## Status
Passed (automated run 2026-10-15)

## Linked Defect (if failed)
None.
