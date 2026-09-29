# TC-MKT-003: Popular products are ordered by total launches across all customers

| Field | Value |
|---|---|
| Test Case ID (required) | TC-MKT-003 |
| Requirement ID (required) | [REQ-MKT-001](../../../docs/02-requirements/FRD/product-recommendations/requirement.md) |
| Acceptance Criterion | [AC-3](../../../docs/02-requirements/FRD/product-recommendations/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-MKT-001](TESTPLAN-MKT-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
Two products; two customers who launch one of them repeatedly.

## Steps
1. Arrange: create product A and product B.
2. Act: record 6 total launches (across two customers) for A, 1 for B.
3. Act: call getRecommendations.
4. Observe the relative order of A and B in the popular list.

## Expected Result
A ranks ahead of B.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/dashboard/service/RecommendationServiceTest.java` — `popularProductsAreOrderedByTotalLaunchesAcrossCustomers`

## Actual Result
The automated tests below passed on 2026-09-28.

## Status
Passed (automated run 2026-09-28)

## Linked Defect (if failed)
None.
