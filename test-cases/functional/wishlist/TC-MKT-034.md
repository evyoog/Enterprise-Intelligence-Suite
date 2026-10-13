# TC-MKT-034: The catalog ratings count approved reviews only

| Field | Value |
|---|---|
| Test Case ID (required) | TC-MKT-034 |
| Requirement ID (required) | [REQ-MKT-004](../../../docs/02-requirements/FRD/wishlist/requirement.md) (C94) |
| Acceptance Criterion | [AC-13](../../../docs/02-requirements/FRD/catalog-showcase/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-MKT-004](TESTPLAN-MKT-004.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A product with two approved reviews (5 and 4) and one pending (1); an unreviewed product; a product with only a pending review.

## Steps
1. Read the catalog ratings.

## Expected Result
4.5 with 2 reviews for the first product; the other two are not in the list.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/wishlist/service/WishlistServiceTest.java` — `theCatalogRatingsCountApprovedReviewsOnlyAndLeaveOutUnreviewedProducts`

## Actual Result
The automated tests passed on 2026-10-13.

## Status
Passed (automated run 2026-10-13)

## Linked Defect (if failed)
None.
