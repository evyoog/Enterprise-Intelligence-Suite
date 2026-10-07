# TC-CAT-031: Admin Content tab: add, reorder, publish, delete

| Field | Value |
|---|---|
| Test Case ID (required) | TC-CAT-031 |
| Requirement ID (required) | [REQ-CAT-004](../../../docs/02-requirements/FRD/product-content/requirement.md) — [acceptance criteria](../../../docs/02-requirements/FRD/product-content/acceptance-criteria.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A platform administrator on an application's edit screen.

## Steps
1. Open the Content tab (also by ?tab=content).
2. Add a video, a datasheet (choose a PDF) and an image (alt text required).
3. Move a video down, publish and unpublish, delete with confirmation.
4. Run the accessibility checks.

## Expected Result
Five labelled sections; each action calls the API with the right item and order; a failed save shows the server's message; no accessibility violations; the product page reflects the changes.

## Automated coverage
- `frontend/src/components/productcontent/ProductContent.test.tsx` (admin tab tests)
- `frontend/src/pages/admin/EditProductPage.test.tsx` (both tests)

## Actual Result
The automated tests above passed on 2026-10-07.

## Status
Passed (automated run 2026-10-07)

## Linked Defect (if failed)
