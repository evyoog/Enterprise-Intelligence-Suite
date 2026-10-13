# TC-MKT-037: The Wishlist page lists saved products newest first and leaves out one no longer in the catalog

| Field | Value |
|---|---|
| Test Case ID (required) | TC-MKT-037 |
| Requirement ID (required) | [REQ-MKT-004](../../../docs/02-requirements/FRD/wishlist/requirement.md) (C94) |
| Acceptance Criterion | [AC-6](../../../docs/02-requirements/FRD/wishlist/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-MKT-004](TESTPLAN-MKT-004.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
Saved ids 3, 99 and 1; products 1 to 3 in the catalog.

## Steps
1. Open `/my/wishlist`.

## Expected Result
Gamma then Alpha; product 99 is not shown; the rating of Alpha is shown.

## Automated coverage
- `frontend/src/pages/WishlistPage.test.tsx` — `shows the saved products…`

## Actual Result
The automated tests passed on 2026-10-13.

## Status
Passed (automated run 2026-10-13)

## Linked Defect (if failed)
None.
