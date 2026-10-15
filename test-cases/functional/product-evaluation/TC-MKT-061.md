# TC-MKT-061: A product alone is compared at its entry plan; a product without plans at its flat price; seats default to 1

| Field | Value |
|---|---|
| Test Case ID (required) | TC-MKT-061 |
| Requirement ID (required) | [REQ-MKT-005](../../../docs/02-requirements/FRD/product-evaluation/requirement.md) (C102) |
| Acceptance Criterion | [AC-13](../../../docs/02-requirements/FRD/product-evaluation/acceptance-criteria.md) |
| Business rule(s) | [BR-EVL-023, BR-EVL-024](../../../docs/02-requirements/FRD/product-evaluation/business-rules.md) |
| Decision | [C102](../../../docs/01-business/roadmap/open-decisions.md#c102) |
| Test Plan | [TESTPLAN-MKT-005](TESTPLAN-MKT-005.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A product with plans Pro (100.00) and Lite (40.00); a product with no plans and a flat price of 10.00.

## Steps
1. Call `GET /products/compare?items={A},{B}` with no `seats`.

## Expected Result
`seats` is 1; item 1 uses the Lite plan at 40.00 (the lowest price); item 2 has no plan and costs 10.00.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/evaluation/service/TrialDemoCompareTest.java` — `aProductOnItsOwnIsComparedAtItsEntryPlanAndAFlatProductAtItsPrice`

The USD currency of a product without a plan is in the code (`CompareService#item`) but not asserted.

## Actual Result
The backend tests (`TrialDemoCompareTest`, 12 tests, and `EvaluationAuthorizationTest`, 2 tests) passed on 2026-10-15; the full backend run of the same day had 876 tests, 0 failures and 28 skipped.

## Status
Passed (automated run 2026-10-15)

## Linked Defect (if failed)
None.
