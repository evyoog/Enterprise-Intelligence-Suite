# TC-CAT-017: Catalog Showcase — All Apps filters, create and delete confirmation

| Field | Value |
|---|---|
| Test Case ID (required) | TC-CAT-017 |
| Requirement ID (required) | [REQ-CAT-003](../../../docs/02-requirements/FRD/catalog-showcase/requirement.md) |
| Acceptance Criterion | [AC-7](../../../docs/02-requirements/FRD/catalog-showcase/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-CAT-003](TESTPLAN-CAT-003.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
Three apps: active, featured on a platform, retired.

## Steps
1. Click Featured, then All.
2. Search for text that matches nothing, then Clear filters.
3. Click Delete on the retired app, then confirm.
4. Click Add App, fill name and price, save.

## Expected Result
The table narrows to the featured app and back; the no-match state appears and clears; delete is not called until confirmed, then the row disappears; the new app is created from the dialog and appears in the list.

## Automated coverage
- `frontend/src/pages/admin/AdminProductsPage.test.tsx` — `filters by status, platform and search, and confirms before deleting`, `creates a new app from the Add App dialog and refreshes the grid`, `shows an empty state when there are no apps`

## Actual Result
The automated tests above passed on 2026-10-03.

## Status
Passed (automated run 2026-10-03)

## Linked Defect (if failed)
None.
