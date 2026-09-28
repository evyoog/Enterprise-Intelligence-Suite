# TC-MKT-007: Rejecting a review keeps it out of ratings

| Field | Value |
|---|---|
| Test Case ID (required) | TC-MKT-007 |
| Requirement ID (required) | [REQ-MKT-002](../../../docs/02-requirements/FRD/product-reviews/requirement.md) |
| Acceptance Criterion | [AC-3](../../../docs/02-requirements/FRD/product-reviews/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-MKT-002](TESTPLAN-MKT-002.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A PENDING review.

## Steps
1. Arrange: a customer submits a review.
2. Act: an admin rejects it.
3. Act: call getRatings for the product.

## Expected Result
The review count stays 0.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/reviews/service/ProductReviewServiceTest.java` — `rejectingAReviewKeepsItOutOfRatings`

## Actual Result
The automated tests below passed on 2026-09-28.

## Status
Passed (automated run 2026-09-28)

## Linked Defect (if failed)
None.
