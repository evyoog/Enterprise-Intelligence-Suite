# TC-MKT-039: The Wishlist page is accessible

| Field | Value |
|---|---|
| Test Case ID (required) | TC-MKT-039 |
| Requirement ID (required) | [REQ-MKT-004](../../../docs/02-requirements/FRD/wishlist/requirement.md) (C94) |
| Acceptance Criterion | [AC-7](../../../docs/02-requirements/FRD/wishlist/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-MKT-004](TESTPLAN-MKT-004.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
Two saved products.

## Steps
1. Run the accessibility check.

## Expected Result
No axe violations.

## Automated coverage
- `frontend/src/pages/WishlistPage.test.tsx` — `has no detectable a11y violations`

## Actual Result
The automated tests passed on 2026-10-13.

## Status
Passed (automated run 2026-10-13)

## Linked Defect (if failed)
None.
