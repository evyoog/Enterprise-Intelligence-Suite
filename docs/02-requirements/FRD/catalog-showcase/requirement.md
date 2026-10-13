# REQ-CAT-003 — Catalog Showcase and Admin UI Redesign

**Status:** Approved
**BRD:** Not specified
**Owner:** Product owner
**Approved by / on:** Product owner / 2026-10-03 ([C66](../../../01-business/roadmap/open-decisions.md#c66)); public catalog page redesigned 2026-10-13 ([C94](../../../01-business/roadmap/open-decisions.md#c94))

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
| REQ-CAT-003.11 | **Catalog banner and category tiles (C94):** the public catalog `/products` opens with a banner (title, subtitle, three trust points, View offerings, Wishlist for signed-in users, Add Product for administrators) and a row of category tiles, each with its product count; "All" shows the total. One category is chosen at a time. | P0 |
| REQ-CAT-003.12 | **Filter panel (C94):** beside the application cards a panel filters by **Platform**, **Pricing model** (Subscription, One-time, Free), **Features** (feature tags, first six with Show more) and, for signed-in users, **Saved products only**. Each option shows how many loaded products it matches; Clear all resets them; on a phone the panel opens from a Filters button. | P0 |
| REQ-CAT-003.13 | **Product card (C94, compacted by [C95](../../../01-business/roadmap/open-decisions.md#c95)):** a compact card: a banner strip (the app's banner image, else a tint of its colour) with the badges **Featured** and **New** (added in the last 30 days) and the wishlist heart; the **app icon** (or initials) overlapping the strip's lower edge; the name with the **From** price (cheapest paid plan, or Free); the category and platform with the **rating and review count** (approved reviews, only when there are some); a two-line description; and the **Subscribe** and **Add to cart** actions ([REQ-MKT-003.10](../cart-checkout/requirement.md)). No feature tags (they stay on the app details page). The heart is specified in [REQ-MKT-004](../wishlist/requirement.md). | P0 |
| REQ-CAT-003.15 | **App icon and banner image (C95):** the app form has two image uploads, **App icon** (square, the existing logo) and **Banner image** (wide, about 1200 × 300 px), each with a preview, an upload button and Remove, using the existing product image upload. The banner image is stored with the product, returned by the product API and shown as the card's banner strip; the Showcase Preview in the form is the catalog's own card. | P0 |
| REQ-CAT-003.14 | **Toolbar (C94):** "Showing N products", search with recent searches, **Sort** by Name, Price, Newest or Rating, and a **Grid / List** switch whose choice is remembered in the browser. | P0 |

## Out of scope
- **Release versions** of an app: the backend has no release data. `products.version` is an edit counter (REQ-CAT-001.1) and is shown only on the edit page, as "Revision N".
- A "Popular" badge or sort, device-platform, deployment and integration filters, and media galleries — no backing data or decision (ratings on cards were added by C94).
- Redesigning the other screens beyond the shared theme (see [C66](../../../01-business/roadmap/open-decisions.md#c66) *Not redesigned*).
- 02.04 Product Content (datasheets, versioned content, videos, case studies).

## Dependencies
- Existing `PlatformService`, `ProductService`, `ProductController`, `PlatformController`, `MANAGE_CATALOG`.
- New public `GET /catalog/platforms` and `GET /catalog/platforms/{id}` ([API](../../../06-api/api-requirements/catalog-showcase.md)). C94: public `GET /products/ratings` and `createdAt` on the product response (same API file).
- New columns ([data model](../../../07-database/data-model/catalog-showcase.md), migration [V019](../../../../database/migrations/V019__catalog_showcase.sql)).
- Screens: [catalog-showcase.md](../../../05-ui/screen-requirements/catalog-showcase.md); design system: [design-system.md](../../../05-ui/screen-requirements/design-system.md).
