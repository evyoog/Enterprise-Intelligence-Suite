# UI requirements — Configurable Claim Mapping

Screens and placement follow the sprint 2026.3.3 development plan: reuse existing pages, layouts and components. Do not add a new layout.

## Screens
| Screen | Route | Roles | Wireframe |
|---|---|---|---|
| Claim mapping fields on the SAML and OIDC provider forms (exact placement Not specified) | Existing provider pages | Organization administrator | Not specified |

## Fields and validation
| Field | Type | Required | Validation | Error message (i18n key) |
|---|---|---|---|---|
| Email claim | Text | No (default applies) | Not specified | Backend message shown as returned |
| First name claim | Text | No (default applies) | Not specified | Backend message shown as returned |
| Last name claim | Text | No (default applies) | Not specified | Backend message shown as returned |
| Display name claim | Text | No (default applies) | Not specified | Backend message shown as returned |

Validation is done by the backend. The UI shows the backend's error message as returned and does not re-implement the rules.

## States
- Empty fields mean the default mapping applies.

## Accessibility and localization
- All user-facing text goes through i18n, in both `en.json` and `es.json`.
- Keyboard operable, and screen-reader labelled controls (workbook function 01.01.02). A jest-axe check runs in the component tests.
