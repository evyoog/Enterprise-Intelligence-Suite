# UI requirements — Catalog Showcase

The screen-by-screen specification is [catalog-showcase.md](../../../05-ui/screen-requirements/catalog-showcase.md); tokens and shared components are in [design-system.md](../../../05-ui/screen-requirements/design-system.md).

## Screens
| Screen | Route | Roles | Wireframe |
|--------|-------|-------|-----------|
| Product Catalog | `/products` | Everyone | Not specified |
| Platform details | `/catalog/platforms/:id` | Everyone | Not specified |
| App details | `/products/:id` | Everyone | Not specified |
| Platforms (admin) | `/admin/platforms` | `MANAGE_CATALOG` | Not specified |
| Create / Edit Platform | `/admin/settings/platform`, `/admin/platforms/:id/edit` | `MANAGE_CATALOG` | Not specified |
| All Apps | `/admin/apps` | `MANAGE_CATALOG` | Not specified |
| Create / Edit App | `/admin/settings/product`, `/admin/products/:id/edit` | `MANAGE_CATALOG` | Not specified |

## Fields and validation
| Field | Type | Required | Validation | Error message (i18n key) |
|-------|------|----------|------------|--------------------------|
| Platform name | text | Yes | 1–255 | `forms.required`, `forms.tooLong` |
| Platform colour | default / HEX | No | `#RRGGBB` | `ui.color.hexError` |
| Platform status | Active / Inactive | Yes | — | — |
| Display order | number | Yes | 0–9999 | `forms.platform.orderError` |
| App accent colour | inherit / HEX | No | `#RRGGBB` | `ui.color.hexError` |
| Feature tag | text | No | ≤ 40, no comma, ≤ 12 tags, no duplicates | `forms.app.tagTooLong`, `forms.app.tagLimit`, `forms.app.tagDuplicate` |
| Documentation / support URL | URL | No | `http(s)://…` | `forms.app.urlError` |
| Price | number | Yes | > 0 | `forms.app.priceError` |
| Pricing tier | group | No | name and price > 0 | `forms.app.tierError` |

## States
- Empty: catalog "No products match your filters" with Clear filters; All Apps "No apps yet" with Add App; Platforms "No platforms yet".
- Loading: skeleton cards and summary values.
- Error: `ErrorState` with the server message and Retry.

## Accessibility and localization
- Whole-card links with a single labelled action (`View details: <name>`); filter chips use `aria-pressed`; colour swatches are buttons with names and `aria-pressed`; form sections are labelled regions; the preview is a labelled complementary region.
- All text in `en.json` and `es.json` (`catalog.*`, `forms.*`, `admin.apps.*`, `admin.platforms.*`, `ui.*`).
