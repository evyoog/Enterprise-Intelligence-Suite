# UI requirements — Platform Administration

## Screens
| Screen | Route | Roles | Wireframe |
|--------|-------|-------|-----------|
| Common settings | `/admin/settings/common` | Platform admin (`MANAGE_PLATFORM_SETTINGS`) | Not specified |

## Fields and validation
| Field | Type | Required | Validation | Error message (i18n key) |
|-------|------|----------|------------|--------------------------|
| Currency toggle | Switch, per row | n/a | n/a | Backend message shown as-is |
| Region code | Text | Yes (to create) | Non-blank, unique | Backend message shown as-is |
| Region name | Text | Yes (to create/update) | Non-blank | n/a |
| Region enabled toggle, delete | Switch / icon button | n/a | Delete refused if assigned | Backend message shown as-is |
| Feature flag key | Text | Yes (to create) | Non-blank, unique | Backend message shown as-is |
| Feature flag enabled toggle, delete | Switch / icon button | n/a | n/a | Backend message shown as-is |

## States
- Empty: "No regions yet." / "No feature flags yet." when either list is empty.
- Loading/Error: each of the four sections (languages, currencies, regions, feature flags) loads and errors independently, with its own inline alert.

## Accessibility and localization
- Every toggle carries an explicit `aria-label` naming the row it controls (e.g. "Enable EUR", "Enable Asia Pacific").
- This admin-only page is plain English text, same as the rest of the (untranslated) admin settings area — no new i18n gap introduced.
