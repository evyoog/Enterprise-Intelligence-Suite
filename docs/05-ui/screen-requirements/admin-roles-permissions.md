# Screen: Roles & permissions (platform)

| Field | Value |
|---|---|
| Requirement | [REQ-TEN-005.11](../../02-requirements/FRD/access-management/requirement.md), keeping every function of [REQ-IAM-003](../../02-requirements/FRD/role-permission-administration/requirement.md) |
| Routes | `/admin/access/roles` (Roles tab), `/admin/access/roles/{id}`, `/admin/access/permissions` (Permissions catalogue tab). The old `/admin/roles` and `/admin/permissions` redirect here once this screen covers all their functions |
| Sidebar | Admin → **Access** → **Roles & permissions** (replaces the separate Roles and Permissions items) |
| Permissions | `MANAGE_ROLES` (roles), `MANAGE_PERMISSIONS` (catalogue) — unchanged |
| Status | Specified, not built (waits for approval) |

Shared components: [access-ui-components.md](access-ui-components.md); presentation rules: [billing-ui-standards.md](billing-ui-standards.md).

## Roles tab (master–detail)
- **Left:** **New role** button; search box; role list with name, scope chip (Platform / Organization), member count, lock icon on protected roles.
- **Right:** role header — name, description, scope chip, **Edit**, **Delete** (disabled with a `ReasonTooltip` for protected roles). Below: permission search and an "Only enabled" filter; permissions grouped by area as `PermissionToggleRow`s (plain-language name, description, technical code in a tooltip).
- `StickySaveBar` for permission changes.
- Validation messages from the existing API (protected roles, organization-scope role names, duplicate names) are shown inline under the field or in the save bar.

## Permissions catalogue tab
- `DataTable`: name, code, area, scope, roles using it; actions **Edit**, **Delete** (disabled with the reason for protected permissions); **New permission**. Create and edit in a dialog, as today.

## States, accessibility, i18n
As [access-management.md](access-management.md): skeletons, backend messages with Retry, toasts; switches with `role="switch"`; keyboard-reachable reasons; axe tests for both tabs and the dialogs; strings under `access.*`.

## API used
Existing `/admin/roles` and `/admin/permissions` (REQ-IAM-003), unchanged.
