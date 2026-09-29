# TC-MKT-005: A submitted review starts pending and is invisible until approved

| Field | Value |
|---|---|
| Test Case ID (required) | TC-MKT-005 |
| Requirement ID (required) | [REQ-MKT-002](../../../docs/02-requirements/FRD/product-reviews/requirement.md) |
| Acceptance Criterion | [AC-1](../../../docs/02-requirements/FRD/product-reviews/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-MKT-002](TESTPLAN-MKT-002.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
None.

## Steps
1. Act: a customer submits a review (rating 4).
2. Observe the returned status.
3. Act: call getRatings for the product.
4. Act: approve the review.
5. Act: call getRatings again.

## Expected Result
Step 2: PENDING. Step 3: reviewCount 0, averageRating null. Step 5: reviewCount 1, averageRating 4.0.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/reviews/service/ProductReviewServiceTest.java` — `aSubmittedReviewStartsPendingAndIsInvisibleUntilApproved`

## Actual Result
The automated tests below passed on 2026-09-28.

## Status
Passed (automated run 2026-09-28)

## Linked Defect (if failed)
None.
