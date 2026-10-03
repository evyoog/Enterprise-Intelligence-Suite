# TC-CAT-013: Catalog Showcase — The Product Catalog shows real data, filters and states

| Field | Value |
|---|---|
| Test Case ID (required) | TC-CAT-013 |
| Requirement ID (required) | [REQ-CAT-003](../../../docs/02-requirements/FRD/catalog-showcase/requirement.md) |
| Acceptance Criterion | [AC-3](../../../docs/02-requirements/FRD/catalog-showcase/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-CAT-003](TESTPLAN-CAT-003.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
Two apps in two categories, one visible platform.

## Steps
1. Open `/products`.
2. Click a category chip.
3. Search for text that matches nothing.

## Expected Result
Summary cards show the counted values; platform and app cards render with Launch and View details; the category filter calls the search with that category and hides platforms without it; the empty state offers Clear filters; Add Product shows only for administrators.

## Automated coverage
- `frontend/src/pages/CatalogShowcase.test.tsx` — `shows real counts, platform cards and app cards`, `filters by category and offers Add Product to administrators`, `shows an empty state with Clear filters`

## Actual Result
The automated tests above passed on 2026-10-03.

## Status
Passed (automated run 2026-10-03)

## Linked Defect (if failed)
None.
