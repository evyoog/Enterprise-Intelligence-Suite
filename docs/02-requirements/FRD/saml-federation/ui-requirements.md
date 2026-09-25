# UI requirements — SAML Federation — Edit Provider

Screens and placement follow the sprint 2026.3.3 development plan: reuse existing pages, layouts and components. Do not add a new layout.

## Screens
| Screen | Route | Roles | Wireframe |
|---|---|---|---|
| Edit action and dialog on the existing SAML providers page | Existing route of `OrganizationSamlProvidersPage` | Users allowed by the backend (`MANAGE_ORGANIZATION`) | Not specified |

## Fields and validation
| Field | Type | Required | Validation | Error message (i18n key) |
|---|---|---|---|---|
| Name | Text | No (unchanged if blank) | Backend | Backend message shown as returned |
| Metadata XML | Multi-line text | Either this, or all three manual fields | Backend | Backend message shown as returned |
| Entity id | Text | With SSO URL and certificate when no metadata | Backend | Backend message shown as returned |
| SSO URL | Text | With entity id and certificate when no metadata | Backend (absolute URL) | Backend message shown as returned |
| Signing certificate (PEM) | Multi-line text | With entity id and SSO URL when no metadata | Backend (X.509) | Backend message shown as returned |

Validation is done by the backend. The UI shows the backend's error message as returned and does not re-implement the rules.

## States
- The edit dialog reuses the existing create form, filled in with the provider's current values.
- Saved: the list shows the provider returned by the PUT call.
- Error: the backend error message is shown and the dialog stays open.

## Accessibility and localization
- All user-facing text goes through i18n, in both `en.json` and `es.json`.
- Keyboard operable, and screen-reader labelled controls (workbook function 01.01.02). A jest-axe check runs in the component tests.
