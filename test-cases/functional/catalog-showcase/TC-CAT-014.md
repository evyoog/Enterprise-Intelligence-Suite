# TC-CAT-014: Catalog Showcase — Platform details page

| Field | Value |
|---|---|
| Test Case ID (required) | TC-CAT-014 |
| Requirement ID (required) | [REQ-CAT-003](../../../docs/02-requirements/FRD/catalog-showcase/requirement.md) |
| Acceptance Criterion | [AC-4](../../../docs/02-requirements/FRD/catalog-showcase/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-CAT-003](TESTPLAN-CAT-003.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A visible platform with one app.

## Steps
1. Open `/catalog/platforms/{id}`.

## Expected Result
Overview, Applications, Features and Product information are shown, with a support call to action.

## Automated coverage
- `frontend/src/pages/CatalogShowcase.test.tsx` — `shows the overview, applications, features and information`

## Actual Result
The automated tests above passed on 2026-10-03.

## Status
Passed (automated run 2026-10-03)

## Linked Defect (if failed)
None.
