# TESTPLAN-MKT-004: Wishlist

| Field | Value |
|---|---|
| Feature ID (required) | FTR-MKT-004 |
| Requirement(s) covered | [REQ-MKT-004](../../../docs/02-requirements/FRD/wishlist/requirement.md); the catalog rating and added date of [REQ-CAT-003.13](../../../docs/02-requirements/FRD/catalog-showcase/requirement.md) |
| Decision | [C94](../../../docs/01-business/roadmap/open-decisions.md#c94) |
| Author | Not specified |
| Status | Approved with the FRD (product owner, 2026-10-13) |

## Scope
Every acceptance criterion in [`acceptance-criteria.md`](../../../docs/02-requirements/FRD/wishlist/acceptance-criteria.md): TC-MKT-031 to TC-MKT-039. The heart on the catalog is covered by TC-CAT-057.

## Approach
Backend: `WishlistServiceTest` (H2-backed Spring context) and `WishlistAuthorizationTest` (MockMvc through the real security chain). Frontend: `WishlistPage.test.tsx` with jest-axe, API mocked.

## Test cases
| Test case | Title | Acceptance criteria | Automated |
|---|---|---|---|
| [TC-MKT-031](TC-MKT-031.md) | Saving a product twice keeps one entry; the list is newest first | AC-1 | Yes |
| [TC-MKT-032](TC-MKT-032.md) | Removing a product keeps the others; removing an unsaved one is harmless | AC-2 | Yes |
| [TC-MKT-033](TC-MKT-033.md) | Each customer sees only their own list; only active products can be saved | AC-3 | Yes |
| [TC-MKT-034](TC-MKT-034.md) | The catalog ratings count approved reviews only | AC-13 (catalog-showcase) | Yes |
| [TC-MKT-035](TC-MKT-035.md) | The product response says when the product was added | AC-13 (catalog-showcase) | Yes |
| [TC-MKT-036](TC-MKT-036.md) | The wishlist needs sign-in; the catalog ratings are public | AC-4, AC-13 (catalog-showcase) | Yes |
| [TC-MKT-037](TC-MKT-037.md) | The Wishlist page lists saved products newest first and leaves out one no longer in the catalog | AC-6 | Yes |
| [TC-MKT-038](TC-MKT-038.md) | Removing from the Wishlist page leads to the empty state | AC-6 | Yes |
| [TC-MKT-039](TC-MKT-039.md) | The Wishlist page is accessible | AC-7 | Yes |

## Blocked or partial
- The V034 migration is checked on PostgreSQL by the schema-loading integration tests; a manual run of the migration file on a real database is still to be done by hand.
