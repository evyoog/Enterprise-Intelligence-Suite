# TC-CAT-057: The heart saves a product, the Wishlist button counts, Saved only filters; a signed-out visitor signs in first

| Field | Value |
|---|---|
| Test Case ID (required) | TC-CAT-057 |
| Requirement ID (required) | [REQ-CAT-003](../../../docs/02-requirements/FRD/catalog-showcase/requirement.md) (C94) |
| Acceptance Criterion | [AC-5](../../../docs/02-requirements/FRD/wishlist/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-CAT-003](TESTPLAN-CAT-003.md) |
| Priority | P1 |
| Type | Functional / UI |
| Automated | Yes |

## Preconditions
Signed in; then signed out.

## Steps
1. Click the heart on a product. 2. Tick Saved products only. 3. Signed out, click the heart.

## Expected Result
The product is saved (API called), the heart is pressed, the Wishlist button shows (1), Saved only shows that product; signed out there is no Wishlist button or Saved filter, nothing is saved and the visitor goes to sign in with a return to `/products`.

## Automated coverage
- `frontend/src/pages/ProductsCatalogPage.test.tsx` — `saves a product with the heart…`, `sends a signed-out visitor to sign in…`

## Actual Result
The automated tests passed on 2026-10-13.

## Status
Passed (automated run 2026-10-13)

## Linked Defect (if failed)
None.
