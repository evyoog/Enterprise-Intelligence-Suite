# TESTPLAN-CAT-003: Catalog Showcase

| Field | Value |
|---|---|
| Requirement | [REQ-CAT-003](../../../docs/02-requirements/FRD/catalog-showcase/requirement.md) |
| Sprint | [2026.4.1](../../../docs/01-business/roadmap/sprints/SPRINT-2026.4.1.md) |
| Decision | [C66](../../../docs/01-business/roadmap/open-decisions.md#c66) |
| Scope | Public catalog API and pages, platform and app forms, All Apps, Platforms list, accessibility |
| Out of scope | Screens that only inherit the theme (C66 *Not redesigned*) — covered by their own existing tests |
| Run | `cd backend && mvn -B verify`; `cd frontend && npm test` |

| Test case | Criterion | Title | Status |
|---|---|---|---|
| [TC-CAT-011](TC-CAT-011.md) | AC-1 | The public catalog lists only visible platforms | Passed |
| [TC-CAT-012](TC-CAT-012.md) | AC-2 | Showcase fields are validated and stored | Passed |
| [TC-CAT-013](TC-CAT-013.md) | AC-3 | The Product Catalog shows real data, filters and states | Passed |
| [TC-CAT-014](TC-CAT-014.md) | AC-4 | Platform details page | Passed |
| [TC-CAT-015](TC-CAT-015.md) | AC-5 | Platform form live preview and validation | Passed |
| [TC-CAT-016](TC-CAT-016.md) | AC-6 | App form sections, existing rules and new fields | Passed |
| [TC-CAT-017](TC-CAT-017.md) | AC-7 | All Apps filters, create and delete confirmation | Passed |
| [TC-CAT-018](TC-CAT-018.md) | AC-8 | Accessibility and colour contrast | Passed |

## C94: public catalog redesign
TC-CAT-048 to TC-CAT-058 cover REQ-CAT-003.11 to .14 (acceptance criteria AC-9 to AC-14). Frontend only, with jest-axe: `catalogFilters.test.ts`, `CatalogComponents.test.tsx`, `ProductsCatalogPage.test.tsx`.

| Test case | Criterion | Title | Status |
|---|---|---|---|
| [TC-CAT-048](TC-CAT-048.md) | AC-10 | The pricing model is worked out from the plans | Passed (automated, 2026-10-13) |
| [TC-CAT-049](TC-CAT-049.md) | AC-11 | The From price is the cheapest paid plan and New means added in the last 30 days | Passed (automated, 2026-10-13) |
| [TC-CAT-050](TC-CAT-050.md) | AC-10 | Filters by platform, pricing model, feature and saved, with facet counts | Passed (automated, 2026-10-13) |
| [TC-CAT-051](TC-CAT-051.md) | AC-12 | Sort by name, price, newest and rating | Passed (automated, 2026-10-13) |
| [TC-CAT-052](TC-CAT-052.md) | AC-9, AC-11, AC-14 | The product card shows badges, rating, tags, From price and the heart | Passed (automated, 2026-10-13) |
| [TC-CAT-053](TC-CAT-053.md) | AC-9, AC-14 | Category tiles show counts and pick one category at a time | Passed (automated, 2026-10-13) |
| [TC-CAT-054](TC-CAT-054.md) | AC-10, AC-14 | The filter panel toggles options, clears them and shows more features | Passed (automated, 2026-10-13) |
| [TC-CAT-055](TC-CAT-055.md) | AC-9 | The catalog shows the banner, the category tiles and every card with its rating | Passed (automated, 2026-10-13) |
| [TC-CAT-056](TC-CAT-056.md) | AC-12 | Category narrows on the server; pricing model and feature narrow the loaded products; sort and list view | Passed (automated, 2026-10-13) |
| [TC-CAT-057](TC-CAT-057.md) | AC-5 (REQ-MKT-004), AC-10 | The heart saves a product, the Wishlist button counts, Saved only filters; a signed-out visitor signs in first | Passed (automated, 2026-10-13) |
| [TC-CAT-058](TC-CAT-058.md) | AC-14 | The empty state offers Clear filters and the catalog is accessible | Passed (automated, 2026-10-13) |

## C95: banner image and narrower cards
TC-CAT-059 to TC-CAT-062 cover REQ-CAT-003.15 and the changed .13 (AC-15, AC-16).

| Test case | Criterion | Title | Status |
|---|---|---|---|
| [TC-CAT-059](TC-CAT-059.md) | AC-15 | The banner image is stored and returned separately from the app icon and can be cleared | Passed (automated, 2026-10-13) |
| [TC-CAT-060](TC-CAT-060.md) | AC-15 | The image upload control has an upload button, a preview, Remove and error reporting | Passed (automated, 2026-10-13) |
| [TC-CAT-061](TC-CAT-061.md) | AC-15 | The app form sends both images with the app and previews them | Passed (automated, 2026-10-13) |
| [TC-CAT-062](TC-CAT-062.md) | AC-16 | The card shows the banner and icon, falls back, and is not clickable in the form preview | Passed (automated, 2026-10-13) |
