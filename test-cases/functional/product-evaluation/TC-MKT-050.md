# TC-MKT-050: An individual starts a free trial: an active trial subscription on the highest plan

| Field | Value |
|---|---|
| Test Case ID (required) | TC-MKT-050 |
| Requirement ID (required) | [REQ-MKT-005](../../../docs/02-requirements/FRD/product-evaluation/requirement.md) (C102) |
| Acceptance Criterion | [AC-2](../../../docs/02-requirements/FRD/product-evaluation/acceptance-criteria.md) |
| Business rule(s) | [BR-EVL-003, BR-EVL-006, BR-EVL-010](../../../docs/02-requirements/FRD/product-evaluation/business-rules.md) |
| Decision | [C102](../../../docs/01-business/roadmap/open-decisions.md#c102) |
| Test Plan | [TESTPLAN-MKT-005](TESTPLAN-MKT-005.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A published product with 7 trial days and two plans (Basic 100.00, Enterprise 900.00); a signed-in individual customer with no subscription to it.

## Steps
1. Call `POST /me/trials` with the product id.
2. Read the returned subscription and the customer's notifications.

## Expected Result
A subscription with `trial` true, status ACTIVE, on the Enterprise plan (the higher price), `autoRenew` false and `expiresAt` about 7 days from now; the customer has a notice "Free trial started: {product}"; no invoice exists for it.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/evaluation/service/TrialDemoCompareTest.java` — `aTrialIsAnActiveSubscriptionOnTheHighestPlanThatEndsAfterTheTrialDaysWithoutRenewing`

The test asserts `trial`, status, the plan, `autoRenew`, the end within 6 to 7 days, the service instance and the notice. It does not assert the 1 seat or that no invoice is generated (the code creates none: `beginTrial` does not call the invoice service).

## Actual Result
The backend tests (`TrialDemoCompareTest`, 12 tests, and `EvaluationAuthorizationTest`, 2 tests) passed on 2026-10-15; the full backend run of the same day had 876 tests, 0 failures and 28 skipped.

## Status
Passed (automated run 2026-10-15)

## Linked Defect (if failed)
None.
