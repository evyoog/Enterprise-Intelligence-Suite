# TC-MKT-038: Removing from the Wishlist page leads to the empty state

| Field | Value |
|---|---|
| Test Case ID (required) | TC-MKT-038 |
| Requirement ID (required) | [REQ-MKT-004](../../../docs/02-requirements/FRD/wishlist/requirement.md) (C94) |
| Acceptance Criterion | [AC-6](../../../docs/02-requirements/FRD/wishlist/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-MKT-004](TESTPLAN-MKT-004.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
One saved product.

## Steps
1. Click the pressed heart.

## Expected Result
The remove call is made; "Your wishlist is empty" and a Browse products link to `/products`.

## Automated coverage
- `frontend/src/pages/WishlistPage.test.tsx` — `removes a product with the heart…`

## Actual Result
The automated tests passed on 2026-10-13.

## Status
Passed (automated run 2026-10-13)

## Linked Defect (if failed)
None.
