# TC-MKT-009: Average rating reflects only approved reviews

| Field | Value |
|---|---|
| Test Case ID (required) | TC-MKT-009 |
| Requirement ID (required) | [REQ-MKT-002](../../../docs/02-requirements/FRD/product-reviews/requirement.md) |
| Acceptance Criterion | [AC-5](../../../docs/02-requirements/FRD/product-reviews/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-MKT-002](TESTPLAN-MKT-002.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
Three reviews on the same product: two approved (5 and 1), one left pending (3).

## Steps
1. Arrange: three customers submit reviews (5, 1, 3) for the same product; the admin approves the first two only.
2. Act: call getRatings for the product.

## Expected Result
reviewCount is 2 and averageRating is 3.0 (only the approved 5 and 1 are counted).

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/reviews/service/ProductReviewServiceTest.java` — `averageRatingReflectsOnlyApprovedReviews`

## Actual Result
The automated tests below passed on 2026-09-28.

## Status
Passed (automated run 2026-09-28)

## Linked Defect (if failed)
None.
