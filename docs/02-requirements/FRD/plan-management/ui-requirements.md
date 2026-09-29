# UI requirements — Plan Management

## Screens
| Screen | Route | Roles | Wireframe |
|--------|-------|-------|-----------|
| Edit app (pricing tiers section) | `/admin/products/:id/edit`, `/admin/settings/product` (create) | Platform admin (`MANAGE_CATALOG`) | Not specified |

## Fields and validation
| Field | Type | Required | Validation | Error message (i18n key) |
|-------|------|----------|------------|--------------------------|
| Currency | Select (USD/EUR/GBP/INR) | No (defaults USD) | Fixed list | n/a |
| Usage limit | Number | No | ≥ 0 | Backend message shown as-is |
| Included features | Text | No | Free text | n/a |
| Usage price | Number | No | ≥ 0 | Backend message shown as-is |
| Overage charge | Number | No | ≥ 0 | Backend message shown as-is |
| Tier pricing (display text) | Text | No | Free text | n/a |

## States
- Empty: every new tier starts with these fields blank/USD — existing behavior for name/price/billing is unchanged.
- Loading/Error: shares the existing product-form submit state (single spinner/alert for the whole form).

## Accessibility and localization
- Same plain MUI `TextField`/`Select` components as the rest of the (untranslated) admin product form — no new accessibility pattern introduced.
