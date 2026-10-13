# TC-MKT-036: The wishlist needs sign-in; the catalog ratings are public

| Field | Value |
|---|---|
| Test Case ID (required) | TC-MKT-036 |
| Requirement ID (required) | [REQ-MKT-004](../../../docs/02-requirements/FRD/wishlist/requirement.md) (C94) |
| Acceptance Criterion | [AC-4, AC-13](../../../docs/02-requirements/FRD/wishlist/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-MKT-004](TESTPLAN-MKT-004.md) |
| Priority | P1 |
| Type | Functional / Security |
| Automated | Yes |

## Preconditions
No sign-in.

## Steps
1. Call GET, PUT and DELETE on `/me/wishlist` without a token. 2. Call `GET /products/ratings`.

## Expected Result
401 for the wishlist calls; 200 for the ratings.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/config/WishlistAuthorizationTest.java`

## Actual Result
The automated tests passed on 2026-10-13.

## Status
Passed (automated run 2026-10-13)

## Linked Defect (if failed)
None.
