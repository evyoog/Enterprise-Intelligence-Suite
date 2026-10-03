# TC-CAT-018: Catalog Showcase — Accessibility and colour contrast

| Field | Value |
|---|---|
| Test Case ID (required) | TC-CAT-018 |
| Requirement ID (required) | [REQ-CAT-003](../../../docs/02-requirements/FRD/catalog-showcase/requirement.md) |
| Acceptance Criterion | [AC-8](../../../docs/02-requirements/FRD/catalog-showcase/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-CAT-003](TESTPLAN-CAT-003.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
The redesigned screens rendered with test data.

## Steps
1. Run axe on the catalog, platform details, platform form, app form, All Apps and Platforms list.
2. Compute the readable text colour for light presets.

## Expected Result
No axe violations. Text colours derived from a showcase colour reach 4.5:1 on white; apps inherit the platform colour unless they have their own.

## Automated coverage
- `frontend/src/pages/CatalogShowcase.test.tsx` — `has no detectable a11y violations`
- `frontend/src/components/admin/ShowcaseForms.test.tsx` — `has no detectable accessibility violations`
- `frontend/src/pages/admin/AdminProductsPage.test.tsx` — `filters by status, platform and search, and confirms before deleting`
- `frontend/src/pages/admin/PlatformsListPage.test.tsx` — `lists platforms in display order with real app counts and catalog visibility`
- `frontend/src/utils/showcaseColor.test.ts` — `derives a light tint and a readable text colour`, `lets an app inherit its platform colour unless it has its own`

## Actual Result
The automated tests above passed on 2026-10-03.

## Status
Passed (automated run 2026-10-03)

## Linked Defect (if failed)
None.
