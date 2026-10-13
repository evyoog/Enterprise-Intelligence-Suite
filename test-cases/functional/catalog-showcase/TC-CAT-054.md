# TC-CAT-054: The filter panel toggles options, clears them and shows more features

| Field | Value |
|---|---|
| Test Case ID (required) | TC-CAT-054 |
| Requirement ID (required) | [REQ-CAT-003](../../../docs/02-requirements/FRD/catalog-showcase/requirement.md) (C94) |
| Acceptance Criterion | [AC-10, AC-14](../../../docs/02-requirements/FRD/catalog-showcase/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-CAT-003](TESTPLAN-CAT-003.md) |
| Priority | P1 |
| Type | Functional / UI |
| Automated | Yes |

## Preconditions
Facets with one platform, two pricing models and eight features.

## Steps
1. Tick a platform, a pricing model and Saved only. 2. Check Clear all. 3. Show more features. 4. Render as a signed-out user.

## Expected Result
Each tick reports the right filter; Clear all is disabled until a filter is set; only six features show until Show more; Saved only is hidden for signed-out users; no axe violations.

## Automated coverage
- `frontend/src/components/catalog/CatalogComponents.test.tsx` — `CatalogFilterPanel`

## Actual Result
The automated tests passed on 2026-10-13.

## Status
Passed (automated run 2026-10-13)

## Linked Defect (if failed)
None.
