# REQ-CAT-003 — Catalog Showcase and Admin UI Redesign

**Status:** Approved
**BRD:** Not specified
**Owner:** Product owner
**Approved by / on:** Product owner / 2026-10-03 ([C66](../../../01-business/roadmap/open-decisions.md#c66))

| Field | Value |
|---|---|
| Sprint | [2026.4.1](../../../01-business/roadmap/sprints/SPRINT-2026.4.1.md) |
| Requirement ID | REQ-CAT-003 |
| Application | [02 Product & Catalog Management](../../../01-business/roadmap/applications/02-product-catalog-management.md) |
| Application code | `APP-CAT` ([DN-5](../../../01-business/roadmap/open-decisions.md#dn-5-application-codes)) |
| Priority | P0: the application is MVP scope ([C4](../../../01-business/roadmap/open-decisions.md#c4), [C5](../../../01-business/roadmap/open-decisions.md#c5)) |
| AI required | No ([C12](../../../01-business/roadmap/open-decisions.md#c12)) |

## Source
The product owner's redesign request of 2026-10-03, recorded as [C66](../../../01-business/roadmap/open-decisions.md#c66). It has no function ID of its own on the application page: it restyles screens of 02.01 Product Lifecycle and 02.03 Plan Management that already exist and adds presentation data to them. Documentation and support **links** touch 02.04.01.02 *Publish documentation* only as a link; 02.04 Product Content (datasheets, versioned content, rich media) stays **not started**.

## Summary
The Product Catalog becomes the reference design for the platform: a clean enterprise SaaS look (indigo primary, light background, white cards, Inter, light sidebar, minimal header). Platforms (product families) and apps are shown on one shared showcase card. Administrators choose a platform colour, whether and where a platform appears in the catalog, an app accent colour (or inherit the platform's), feature tags, and documentation and support links, and see a live preview of the card while editing. Every number on the catalog is counted from real data; nothing is invented, and data the backend does not have (release versions, ratings) is not shown.

## Actors
- Visitor and customer (catalog, platform details, app details — public, read-only)
- Platform administrator (holding `MANAGE_CATALOG`, the existing gate on platform and product mutation)

## Functional requirements
| ID | Requirement | Priority |
|---|---|---|
| REQ-CAT-003.1 | The catalog lists platforms that are **Active** and **shown in catalog**, sorted by display order then name, each with its real count of active apps, the union of those apps' categories and feature tags. | P0 |
| REQ-CAT-003.2 | A public platform details page shows the platform's overview, its active apps, all of their feature tags and a product-information list. A hidden or inactive platform returns "not found". | P0 |
| REQ-CAT-003.3 | An administrator can set a platform's colour (default or custom HEX), status (Active/Inactive), "show in catalog" and display order (0–9999). | P0 |
| REQ-CAT-003.4 | An administrator can set an app's accent colour or leave it empty to inherit the first assigned platform's colour (else the default #6366F1). | P0 |
| REQ-CAT-003.5 | An administrator can give an app up to 12 feature tags of up to 40 characters each; tags are trimmed, de-duplicated (case-insensitive) and may not contain commas. | P0 |
| REQ-CAT-003.6 | An administrator can set an app's documentation URL and support URL (http/https); the app details page links to them when present. | P0 |
| REQ-CAT-003.7 | The platform and app create/edit forms are grouped into titled sections and show a live **Showcase Preview** using the same card as the catalog. | P0 |
| REQ-CAT-003.8 | All Apps lists every app (including Inactive and Retired) with status/featured filters, a platform filter, search and a table or card view; deleting asks for confirmation. | P0 |
| REQ-CAT-003.9 | Every redesigned screen has loading, empty and error states, is keyboard and screen-reader accessible (WCAG 2.1 AA contrast), responsive down to phone width, and translated (en, es). | P0 |
| REQ-CAT-003.10 | Existing routes, API fields, authentication and permissions are unchanged; all new fields are optional and additive. | P0 |

## Out of scope
- **Release versions** of an app: the backend has no release data. `products.version` is an edit counter (REQ-CAT-001.1) and is shown only on the edit page, as "Revision N".
- Ratings on catalog cards, "Popular" sorting and media galleries — no backing data or decision.
- Redesigning the other screens beyond the shared theme (see [C66](../../../01-business/roadmap/open-decisions.md#c66) *Not redesigned*).
- 02.04 Product Content (datasheets, versioned content, videos, case studies).

## Dependencies
- Existing `PlatformService`, `ProductService`, `ProductController`, `PlatformController`, `MANAGE_CATALOG`.
- New public `GET /catalog/platforms` and `GET /catalog/platforms/{id}` ([API](../../../06-api/api-requirements/catalog-showcase.md)).
- New columns ([data model](../../../07-database/data-model/catalog-showcase.md), migration [V019](../../../../database/migrations/V019__catalog_showcase.sql)).
- Screens: [catalog-showcase.md](../../../05-ui/screen-requirements/catalog-showcase.md); design system: [design-system.md](../../../05-ui/screen-requirements/design-system.md).
