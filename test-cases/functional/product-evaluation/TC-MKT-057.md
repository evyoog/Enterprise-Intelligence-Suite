# TC-MKT-057: Buying during a trial turns it into a normal subscription that starts after payment

| Field | Value |
|---|---|
| Test Case ID (required) | TC-MKT-057 |
| Requirement ID (required) | [REQ-MKT-005](../../../docs/02-requirements/FRD/product-evaluation/requirement.md) (C102) |
| Acceptance Criterion | [AC-9](../../../docs/02-requirements/FRD/product-evaluation/acceptance-criteria.md) |
| Business rule(s) | [BR-EVL-015](../../../docs/02-requirements/FRD/product-evaluation/business-rules.md) |
| Decision | [C102](../../../docs/01-business/roadmap/open-decisions.md#c102) |
| Test Plan | [TESTPLAN-MKT-005](TESTPLAN-MKT-005.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A running trial of an individual; a paid plan of the product.

## Steps
1. Call `SubscriptionService#subscribeFromCart` for the product and the paid plan (the call checkout makes).
2. Read the subscription.

## Expected Result
The same subscription row has `trial` false, status PENDING_SUBSCRIPTION (until paid) and `autoRenew` true.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/evaluation/service/TrialDemoCompareTest.java` — `buyingDuringATrialTurnsItIntoANormalSubscriptionThatStartsAfterPayment`

**Not covered by an automated test:** a free plan (starts ACTIVE at once), `subscribe`, and the organization purchase (`activateOrganizationSubscription`). The cart check that precedes this call is [TC-MKT-058](TC-MKT-058.md), which now passes for a running trial too.

## Actual Result
The backend tests (`TrialDemoCompareTest`, 12 tests, and `EvaluationAuthorizationTest`, 2 tests) passed on 2026-10-15; the full backend run of the same day had 876 tests, 0 failures and 28 skipped.

## Status
Passed (automated run 2026-10-15)

## Linked Defect (if failed)
None.
