# UI requirements — Privileged Access (User and Organization Administrator)

Screens and placement follow the sprint 2026.3.3 development plan: reuse existing pages, layouts and components. Do not add a new layout.

## Screens
| Screen | Route | Roles | Wireframe |
|---|---|---|---|
| "Request elevated access" and "My requests" section | Existing `SecuritySettingsPage` route | Any signed-in user | Not specified |
| Organization privileged-access approvals | An existing organization page, reusing the table and dialog approach of `AdminPrivilegedAccessPage` | Users allowed by the backend (`MANAGE_PRIVILEGED_ACCESS`) | Not specified |

## Fields and validation
| Field | Type | Required | Validation | Error message (i18n key) |
|---|---|---|---|---|
| Permission | Dropdown filled from `GET /me/privileged-access/requestable-permissions` (C24) | Yes | Backend (recognized; not `MANAGE_PRIVILEGED_ACCESS`) | Backend message shown as returned |
| Justification | Text | Yes | Backend (not blank) | Backend message shown as returned |
| Duration (minutes) | Number | Yes | Backend: 1–480 | Backend message shown as returned |
| Decision note | Text | No | - | - |

Validation is done by the backend. The UI shows the backend's error message as returned and does not re-implement the rules.

## States
- My requests: permission, status / effective status (including EXPIRED), requested duration, decided time, decision note, expiry.
- Withdraw is offered for the requester's own PENDING or active requests. The backend is the final check.
- Approvals: pending requests with permission, justification, requested duration and requested time; approve, reject and revoke with an optional note.
- Forbidden (403) on the approvals view: handled the way `BusinessDashboardPage` handles a 403.
- Error: the backend error message is shown.

## Accessibility and localization
- All user-facing text goes through i18n, in both `en.json` and `es.json`.
- Keyboard operable, and screen-reader labelled controls (workbook function 01.01.02). A jest-axe check runs in the component tests.
