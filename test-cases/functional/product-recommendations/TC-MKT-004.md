# TC-MKT-004: An inactive product never appears in the popular list

| Field | Value |
|---|---|
| Test Case ID (required) | TC-MKT-004 |
| Requirement ID (required) | [REQ-MKT-001](../../../docs/02-requirements/FRD/product-recommendations/requirement.md) |
| Acceptance Criterion | [AC-4](../../../docs/02-requirements/FRD/product-recommendations/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-MKT-001](TESTPLAN-MKT-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A product with recorded launches, later set INACTIVE.

## Steps
1. Arrange: create a product, record a launch for it, then set its status to INACTIVE.
2. Act: call getRecommendations.
3. Observe the popular list.

## Expected Result
The product is absent from the popular list.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/dashboard/service/RecommendationServiceTest.java` — `anInactiveProductNeverAppearsInThePopularList`

## Actual Result
The automated tests below passed on 2026-09-28.

## Status
Passed (automated run 2026-09-28)

## Linked Defect (if failed)
None.
