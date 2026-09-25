# UI requirements — OIDC Identity-Provider Federation

Screens and placement follow the sprint 2026.3.3 development plan: reuse existing pages, layouts and components. Do not add a new layout.

## Screens
| Screen | Route | Roles | Wireframe |
|---|---|---|---|
| OIDC providers, alongside the existing `OrganizationSamlProvidersPage` (exact placement Not specified) | Not specified | Organization administrator | Not specified |

## Fields and validation
| Field | Type | Required | Validation | Error message (i18n key) |
|---|---|---|---|---|
| Provider fields | Not specified | Not specified | Backend | Backend message shown as returned |

Validation is done by the backend. The UI shows the backend's error message as returned and does not re-implement the rules.

## States
- Mirrors the SAML providers page: list, create, edit, enable, disable, test, delete.
- The client secret is never shown back in full after it is saved (Not specified: exact masking).

## Accessibility and localization
- All user-facing text goes through i18n, in both `en.json` and `es.json`.
- Keyboard operable, and screen-reader labelled controls (workbook function 01.01.02). A jest-axe check runs in the component tests.
