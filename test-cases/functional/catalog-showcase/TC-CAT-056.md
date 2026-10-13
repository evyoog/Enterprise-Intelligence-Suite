# TC-CAT-056: Category narrows on the server; pricing model and feature narrow the loaded products; sort and list view

| Field | Value |
|---|---|
| Test Case ID (required) | TC-CAT-056 |
| Requirement ID (required) | [REQ-CAT-003](../../../docs/02-requirements/FRD/catalog-showcase/requirement.md) (C94) |
| Acceptance Criterion | [AC-12](../../../docs/02-requirements/FRD/catalog-showcase/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-CAT-003](TESTPLAN-CAT-003.md) |
| Priority | P1 |
| Type | Functional / UI |
| Automated | Yes |

## Preconditions
Three products in two categories.

## Steps
1. Click the CRM tile, then All. 2. Tick Free, then Clear all and tick a feature. 3. Sort by Newest, then Highest rated. 4. Switch to List view.

## Expected Result
The search is called with the category; Free keeps one product; the feature keeps the right one; the order follows the sort; the list view is pressed and remembered.

## Automated coverage
- `frontend/src/pages/ProductsCatalogPage.test.tsx` — `narrows by category…`, `sorts by newest and by rating…`

## Actual Result
The automated tests passed on 2026-10-13.

## Status
Passed (automated run 2026-10-13)

## Linked Defect (if failed)
None.
