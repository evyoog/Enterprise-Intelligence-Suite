# UI requirements — Member Role Assignment

Screens and placement follow the sprint 2026.3.3 development plan: reuse existing pages, layouts and components. Do not add a new layout.

## Screens
| Screen | Route | Roles | Wireframe |
|---|---|---|---|
| Organization member list with a role selector, on an existing organization page | Existing route of that page | Users allowed by the backend (`MANAGE_USERS`) | Not specified |

## Fields and validation
| Field | Type | Required | Validation | Error message (i18n key) |
|---|---|---|---|---|
| Name | Text (read-only) | - | - | - |
| Email | Text (read-only) | - | - | - |
| Status | Text (read-only) | - | - | - |
| Organization role | Selector: `ORG_ADMIN`, `MEMBER` | Yes | Backend | Backend message shown as returned (for example, last administrator) |

Validation is done by the backend. The UI shows the backend's error message as returned and does not re-implement the rules.

## States
- Loading: the existing loading pattern while `GET /organization/me/users` loads.
- Empty: Not specified.
- Forbidden (403): handled the way `BusinessDashboardPage` handles a 403.
- Error: the backend error message is shown and the selector keeps the member's current role.
- Saved: the row shows the role returned by the PATCH call.

## Accessibility and localization
- All user-facing text goes through i18n, in both `en.json` and `es.json`.
- Keyboard operable, and screen-reader labelled controls (workbook function 01.01.02). A jest-axe check runs in the component tests.
