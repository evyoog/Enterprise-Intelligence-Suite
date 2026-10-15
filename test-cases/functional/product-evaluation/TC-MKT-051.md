# TC-MKT-051: One trial per customer and product, ever; only where the product offers one

| Field | Value |
|---|---|
| Test Case ID (required) | TC-MKT-051 |
| Requirement ID (required) | [REQ-MKT-005](../../../docs/02-requirements/FRD/product-evaluation/requirement.md) (C102) |
| Acceptance Criterion | [AC-3](../../../docs/02-requirements/FRD/product-evaluation/acceptance-criteria.md) |
| Business rule(s) | [BR-EVL-003, BR-EVL-004, BR-EVL-005](../../../docs/02-requirements/FRD/product-evaluation/business-rules.md) |
| Decision | [C102](../../../docs/01-business/roadmap/open-decisions.md#c102) |
| Test Plan | [TESTPLAN-MKT-005](TESTPLAN-MKT-005.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A published product with trial days; a published product with none; a customer who has started a trial; a customer who subscribed.

## Steps
1. The customer with a trial starts a second trial of the same product.
2. Start a trial of the product that has no trial days.
3. The customer who subscribed (any status) starts a trial.
4. Start a trial of a product that is not published, and of an organization-only product as an individual.

## Expected Result
Steps 1 and 3: 400 "You already have, or had, a subscription to {product}, so a free trial is not available." Step 2: 400 "{product} does not offer a free trial." Step 4: 404 "Product not found" and 409 "{product} is available to organizations only."

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/evaluation/service/TrialDemoCompareTest.java` — `oneTrialPerCustomerPerProductEverAndOnlyWhereTheProductOffersIt`

**Not covered by an automated test:** the unpublished product (404) and the audience rule (409).

## Actual Result
The backend tests (`TrialDemoCompareTest`, 12 tests, and `EvaluationAuthorizationTest`, 2 tests) passed on 2026-10-15; the full backend run of the same day had 876 tests, 0 failures and 28 skipped.

## Status
Passed (automated run 2026-10-15)

## Linked Defect (if failed)
None.
