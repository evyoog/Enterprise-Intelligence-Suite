# TC-CAT-011: Catalog Showcase — The public catalog lists only visible platforms

| Field | Value |
|---|---|
| Test Case ID (required) | TC-CAT-011 |
| Requirement ID (required) | [REQ-CAT-003](../../../docs/02-requirements/FRD/catalog-showcase/requirement.md) |
| Acceptance Criterion | [AC-1](../../../docs/02-requirements/FRD/catalog-showcase/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-CAT-003](TESTPLAN-CAT-003.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
Platforms: one ACTIVE and shown with two active apps and one inactive app; one INACTIVE; one hidden.

## Steps
1. Call `GET /api/catalog/platforms` without signing in.
2. Call `GET /api/catalog/platforms/{id}` for the hidden platform.

## Expected Result
The visible platform is listed with `appCount` 2, categories and tags from the active apps only; the inactive and hidden platforms are absent; the hidden platform's details return 404; display order sorts the list.

## Automated coverage
- `backend/.../platform/controller/CatalogControllerTest.java` — `theCatalogListsVisiblePlatformsWithCountsFromTheirActiveApps`, `displayOrderSortsTheCatalog`

## Actual Result
The automated tests above passed on 2026-10-03.

## Status
Passed (automated run 2026-10-03)

## Linked Defect (if failed)
None.
