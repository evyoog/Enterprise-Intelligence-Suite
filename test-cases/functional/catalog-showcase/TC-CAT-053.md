# TC-CAT-053: Category tiles show counts and pick one category at a time

| Field | Value |
|---|---|
| Test Case ID (required) | TC-CAT-053 |
| Requirement ID (required) | [REQ-CAT-003](../../../docs/02-requirements/FRD/catalog-showcase/requirement.md) (C94) |
| Acceptance Criterion | [AC-9, AC-14](../../../docs/02-requirements/FRD/catalog-showcase/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-CAT-003](TESTPLAN-CAT-003.md) |
| Priority | P1 |
| Type | Functional / UI |
| Automated | Yes |

## Preconditions
Category facets ERP 8 and CRM 1, total 9.

## Steps
1. Render the tiles with ERP selected. 2. Click All, then CRM.

## Expected Result
All 9 products, ERP 8 products (pressed), 1 product; All clears the category; CRM selects it; no axe violations.

## Automated coverage
- `frontend/src/components/catalog/CatalogComponents.test.tsx` — `CategoryTiles`

## Actual Result
The automated tests passed on 2026-10-13.

## Status
Passed (automated run 2026-10-13)

## Linked Defect (if failed)
None.
