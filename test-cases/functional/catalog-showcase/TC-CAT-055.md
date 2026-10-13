# TC-CAT-055: The catalog shows the banner, the category tiles and every card with its rating

| Field | Value |
|---|---|
| Test Case ID (required) | TC-CAT-055 |
| Requirement ID (required) | [REQ-CAT-003](../../../docs/02-requirements/FRD/catalog-showcase/requirement.md) (C94) |
| Acceptance Criterion | [AC-9](../../../docs/02-requirements/FRD/catalog-showcase/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-CAT-003](TESTPLAN-CAT-003.md) |
| Priority | P1 |
| Type | Functional / UI |
| Automated | Yes |

## Preconditions
Three products; a rating for one.

## Steps
1. Open `/products`.

## Expected Result
The banner title, the All tile with 3 products and the ERP tile, "Showing 3 products", the cards in name order and the rating of the rated product.

## Automated coverage
- `frontend/src/pages/ProductsCatalogPage.test.tsx` — `shows the hero, the category tiles…`

## Actual Result
The automated tests passed on 2026-10-13.

## Status
Passed (automated run 2026-10-13)

## Linked Defect (if failed)
None.
