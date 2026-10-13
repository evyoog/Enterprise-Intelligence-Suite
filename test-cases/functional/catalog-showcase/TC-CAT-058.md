# TC-CAT-058: The empty state offers Clear filters and the catalog is accessible

| Field | Value |
|---|---|
| Test Case ID (required) | TC-CAT-058 |
| Requirement ID (required) | [REQ-CAT-003](../../../docs/02-requirements/FRD/catalog-showcase/requirement.md) (C94) |
| Acceptance Criterion | [AC-14](../../../docs/02-requirements/FRD/catalog-showcase/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-CAT-003](TESTPLAN-CAT-003.md) |
| Priority | P1 |
| Type | Functional / UI |
| Automated | Yes |

## Preconditions
Filters that match nothing.

## Steps
1. Run the accessibility check. 2. Tick Free and a feature no free product has. 3. Click Clear filters.

## Expected Result
No axe violations; "No products found" with Clear filters; clearing restores all 3 products.

## Automated coverage
- `frontend/src/pages/ProductsCatalogPage.test.tsx` — `shows an empty state…`

## Actual Result
The automated tests passed on 2026-10-13.

## Status
Passed (automated run 2026-10-13)

## Linked Defect (if failed)
None.
