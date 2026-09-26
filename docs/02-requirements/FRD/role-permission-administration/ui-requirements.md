# UI requirements — Role and Permission Administration

Screens and placement follow the sprint 2026.3.3 development plan: reuse existing pages, layouts and components. Do not add a new layout.

## Screens
| Screen | Route | Roles | Wireframe |
|---|---|---|---|
| Roles list, create and edit | Under the existing `/admin` layout (exact path Not specified) | Platform administrator | Not specified |
| Permissions list, create and edit | Under the existing `/admin` layout (exact path Not specified) | Platform administrator | Not specified |
| Navigation entries for both pages | Administration section of the signed-in sidebar (`appNavigation.ts`; `AdminLayout` before 2026-09-26) | Platform administrator | Not specified |

## Fields and validation
| Field | Type | Required | Validation | Error message (i18n key) |
|---|---|---|---|---|
| Role name | Text | Yes (create only) | Backend (unique; organization-scope name rule) | Backend message shown as returned |
| Role scope | Selector: `PLATFORM`, `ORGANIZATION` | Yes (create only) | Backend | Backend message shown as returned |
| Role description | Text | No | - | - |
| Role permissions | Multi-select of existing permissions | No | Backend (ids must exist) | Backend message shown as returned |
| Permission name | Text | Yes (create only) | Backend (unique) | Backend message shown as returned |
| Permission description | Text | No | - | - |

Validation is done by the backend. The UI shows the backend's error message as returned and does not re-implement the rules.

## States
- Loading: the existing admin loading pattern.
- System-managed roles and permissions are marked as such, using `systemManaged` from the response. Their delete is still sent to the backend, and its refusal message is shown.
- Name and scope fields are read-only when editing (they cannot change after creation).
- Error: the backend error message is shown.

## Accessibility and localization
- All user-facing text goes through i18n, in both `en.json` and `es.json`.
- Keyboard operable, and screen-reader labelled controls (workbook function 01.01.02). A jest-axe check runs in the component tests.
