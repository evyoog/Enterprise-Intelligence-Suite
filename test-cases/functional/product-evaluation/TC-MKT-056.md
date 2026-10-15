# TC-MKT-056: A trial that ends expires at once with no grace period and its instance is deprovisioned

| Field | Value |
|---|---|
| Test Case ID (required) | TC-MKT-056 |
| Requirement ID (required) | [REQ-MKT-005](../../../docs/02-requirements/FRD/product-evaluation/requirement.md) (C102) |
| Acceptance Criterion | [AC-8](../../../docs/02-requirements/FRD/product-evaluation/acceptance-criteria.md) |
| Business rule(s) | [BR-EVL-013, BR-EVL-017](../../../docs/02-requirements/FRD/product-evaluation/business-rules.md) |
| Decision | [C102](../../../docs/01-business/roadmap/open-decisions.md#c102) |
| Test Plan | [TESTPLAN-MKT-005](TESTPLAN-MKT-005.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A running trial of an individual whose `expiresAt` has passed by 5 minutes.

## Steps
1. Run `SubscriptionService#expireOverdueSubscriptions` (the hourly job does).
2. Read the subscription, its service instance and the customer's notifications.

## Expected Result
Status EXPIRED, no `graceEndsAt`; the instance is DEPROVISIONED; the notice "Free trial ended: {product}"; `TRIAL_EXPIRED` is in the audit log and a `SubscriptionChanged` event with status EXPIRED is in the outbox.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/evaluation/service/TrialDemoCompareTest.java` — `aTrialThatEndsExpiresAtOnceWithNoGraceAndTheInstanceIsDeprovisioned`

The test asserts the status, no grace date, the instance and the notice. **Not covered by an automated test:** the `TRIAL_EXPIRED` audit entry and the `SubscriptionChanged` event (the code writes both in `SubscriptionService#expireTrial`).

## Actual Result
The backend tests (`TrialDemoCompareTest`, 12 tests, and `EvaluationAuthorizationTest`, 2 tests) passed on 2026-10-15; the full backend run of the same day had 876 tests, 0 failures and 28 skipped.

## Status
Passed (automated run 2026-10-15)

## Linked Defect (if failed)
None.
