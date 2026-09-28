# TC-MKT-001: A featured active product appears in the featured list

| Field | Value |
|---|---|
| Test Case ID (required) | TC-MKT-001 |
| Requirement ID (required) | [REQ-MKT-001](../../../docs/02-requirements/FRD/product-recommendations/requirement.md) |
| Acceptance Criterion | [AC-1](../../../docs/02-requirements/FRD/product-recommendations/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-MKT-001](TESTPLAN-MKT-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An ACTIVE product marked featured.

## Steps
1. Arrange: create a product with status ACTIVE and featured=true.
2. Act: call getRecommendations.
3. Observe the featured list.

## Expected Result
The product is in the featured list.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/dashboard/service/RecommendationServiceTest.java` — `featuredProductsAreReturned`

## Actual Result
The automated tests below passed on 2026-09-28.

## Status
Passed (automated run 2026-09-28)

## Linked Defect (if failed)
None.
