# UI requirements — Product Recommendations

## Screens
| Screen | Route | Roles | Wireframe |
|--------|-------|-------|-----------|
| Recommendations rails | `/my/products` (dashboard widget) | Any authenticated customer | Not specified |
| Featured toggle | `/admin/apps` product edit form | `MANAGE_CATALOG` | Not specified |

The dashboard's existing customizable-widget mechanism (`MyProductsPage`) gains a "Recommended for you" widget, rendering a Featured rail and a Popular rail (each hidden when empty) — same visual pattern as the existing Recently used/Favorites rails, minus the favorite-toggle/launch actions those carry (a recommendation isn't yet something the customer has access to).

## Fields and validation
| Field | Type | Required | Validation | Error message (i18n key) |
|-------|------|----------|------------|--------------------------|
| Featured | Switch | No (defaults off) | - | - |

## States
- Empty: the widget renders nothing when both lists are empty (no empty-state message — this is a supplementary widget, not a primary one)
- Loading: handled silently (the rails simply appear once loaded, same as other dashboard widgets)
- Error: a failed load is treated as "nothing to recommend" rather than shown as an error — this is not critical-path data

## Accessibility and localization
- Rail section headings use `component="h5"` to keep heading order valid under the page's `h4` title.
- All new admin-form copy ("Featured") follows the existing product form's plain-English style; no separate i18n keys were needed since the admin console's older forms (this one included) are not yet i18n'd.
