# TC-MKT-006: Submitting again edits the same review and resets it to pending

| Field | Value |
|---|---|
| Test Case ID (required) | TC-MKT-006 |
| Requirement ID (required) | [REQ-MKT-002](../../../docs/02-requirements/FRD/product-reviews/requirement.md) |
| Acceptance Criterion | [AC-2](../../../docs/02-requirements/FRD/product-reviews/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-MKT-002](TESTPLAN-MKT-002.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An approved review.

## Steps
1. Arrange: a customer submits and an admin approves a review (rating 3).
2. Act: the same customer submits again for the same product (rating 5).
3. Observe the returned id/status.
4. Act: call getRatings for the product.

## Expected Result
The id is unchanged, status is PENDING again, and the product's review count is back to 0.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/reviews/service/ProductReviewServiceTest.java` — `submittingAgainEditsTheSameReviewAndResetsItToPending`

## Actual Result
The automated tests below passed on 2026-09-28.

## Status
Passed (automated run 2026-09-28)

## Linked Defect (if failed)
None.
