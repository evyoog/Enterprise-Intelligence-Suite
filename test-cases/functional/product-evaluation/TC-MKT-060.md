# TC-MKT-060: Comparing plans multiplies each price by the seats in the item's own currency

| Field | Value |
|---|---|
| Test Case ID (required) | TC-MKT-060 |
| Requirement ID (required) | [REQ-MKT-005](../../../docs/02-requirements/FRD/product-evaluation/requirement.md) (C102) |
| Acceptance Criterion | [AC-12](../../../docs/02-requirements/FRD/product-evaluation/acceptance-criteria.md) |
| Business rule(s) | [BR-EVL-022, BR-EVL-025](../../../docs/02-requirements/FRD/product-evaluation/business-rules.md) |
| Decision | [C102](../../../docs/01-business/roadmap/open-decisions.md#c102) |
| Test Plan | [TESTPLAN-MKT-005](TESTPLAN-MKT-005.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
Two published products: A with plans Pro (100.00) and Lite (40.00), B (trial 14 days) with plan Team (250.50); both plans in INR.

## Steps
1. Call `GET /products/compare?items={A}:{Pro},{B}:{Team}&seats=3` with no token.

## Expected Result
Two items in the order asked; item 1 plan Pro, subtotal 300.00; item 2 plan Team, subtotal 751.50 INR, `trialDays` 14; each item's tax status is `NOT_SIGNED_IN`.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/evaluation/service/TrialDemoCompareTest.java` — `comparingPlansMultipliesThePriceBySeatsInEachItemsOwnCurrency`

**Not covered by an automated test:** two plans of the same product in one comparison (allowed by the parser), and items in two different currencies.

## Actual Result
The backend tests (`TrialDemoCompareTest`, 12 tests, and `EvaluationAuthorizationTest`, 2 tests) passed on 2026-10-15; the full backend run of the same day had 876 tests, 0 failures and 28 skipped.

## Status
Passed (automated run 2026-10-15)

## Linked Defect (if failed)
None.
