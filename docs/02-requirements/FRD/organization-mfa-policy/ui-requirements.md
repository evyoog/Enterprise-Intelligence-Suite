# UI requirements — Organization MFA Policy

Screens and placement follow the sprint 2026.3.3 development plan: reuse existing pages, layouts and components. Do not add a new layout.

## Screens
| Screen | Route | Roles | Wireframe |
|---|---|---|---|
| Organization MFA setting, on Organization settings (`/organization/settings#mfa`, moved from `BusinessDashboardPage` by C69) — originally: on an existing organization page that already shows organization settings (`BusinessDashboardPage` or `SecuritySettingsPage`, whichever already shows organization settings) | Existing route of that page | Users allowed by the backend (`MANAGE_ORGANIZATION`) | Not specified |

## Fields and validation
| Field | Type | Required | Validation | Error message (i18n key) |
|---|---|---|---|---|
| Require MFA for all members | On/off control | Yes | None in the UI | Backend message shown as returned |

Validation is done by the backend. The UI shows the backend's error message as returned and does not re-implement the rules.

## States
- Loading: show the existing loading pattern of the host page while `GET /organization/me` loads.
- Current value: taken from `mfaRequired` on `GET /organization/me`.
- Forbidden (403): handled the way `BusinessDashboardPage` handles a 403.
- Error: the backend error message is shown.
- Saved: the control reflects the value returned by `PATCH /organization/me/mfa-policy`.

## Accessibility and localization
- All user-facing text goes through i18n, in both `en.json` and `es.json`.
- Keyboard operable, and screen-reader labelled controls (workbook function 01.01.02). A jest-axe check runs in the component tests.
- Explain in the UI text that the change applies at each member's next login (business rule 5).
