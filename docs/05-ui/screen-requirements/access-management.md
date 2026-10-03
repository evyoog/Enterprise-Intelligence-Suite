# Screen: Access management

| Field | Value |
|---|---|
| Requirement | [REQ-TEN-005](../../02-requirements/FRD/access-management/requirement.md) |
| Decision | [C65](../../01-business/roadmap/open-decisions.md#c65); placement per [C44](../../01-business/roadmap/open-decisions.md#c44); theme [C45](../../01-business/roadmap/open-decisions.md#c45) |
| Routes | `/organization/access` (Members), `/organization/access/members/{memberId}` (member panel open), `/organization/access/roles/{role}` (Role defaults), `/organization/access/activity` |
| Sidebar | Organization → **Access management** (people-with-shield icon), shown to organization admins and delegated administrators (*Manage access*) |
| Also reached from | The member list on the business dashboard and each member row ("Manage access") |
| Status | Specified, not built (waits for approval) |

Shared presentation: [billing-ui-standards.md](billing-ui-standards.md) (status chips, `DataTable`, `FilterBar`, loading/empty/error states, toasts, responsive layout). `DataTable` and `FilterBar` do not exist yet; they are created as shared components when this screen is built. Shared access components: [access-ui-components.md](access-ui-components.md). Light and dark mode come from the theme.

## Page frame
- Breadcrumbs: Organization › Access management › (tab) › (member).
- `PageHeader` with icon (people-with-shield), area "Organization", title "Access management", description "Control what each member can do and which products they can open".
- Tabs: **Members**, **Role defaults**, **Activity** (the tab is in the URL).

## Members tab
- **FilterBar:** search (name or email); filters: role, product, "Has custom access", "Last reviewed" (never / more than 90 days / any); **Clear filters**. **Bulk change** button, enabled when rows are selected.
- **DataTable** (server-paged, 20 per page), columns:

| Column | Content |
|---|---|
| Select | Checkbox (header checkbox selects the page) |
| Member | Avatar, name (bold), email underneath |
| Role | Chip: *Organization admin* (primary) or *Member* (neutral) |
| Products | Up to 3 product icons with names in tooltips, then "+N" |
| Custom access | Badge "2 custom" when overrides exist; empty otherwise |
| Last reviewed | Date, or "Never" |
| Status | Chip Active / Suspended |
| Actions | **Manage access** |

- Clicking a row or **Manage access** opens the member access panel and updates the URL.
- Empty: "No members match these filters" with **Clear filters**.

## Member access panel
Right-side drawer (560 px) on desktop, full screen below 900 px. Focus moves into it and returns to the row on close.

- **Header:** avatar, name, email; **role selector** (Organization admin / Member) with the last-admin guard; "Last reviewed {date} by {name}" and **Mark as reviewed**.
- **Banner** when the panel is read-only: "You cannot change your own access" or "Only organization admins can change an administrator's access".
- **Section "Product access":** a `ProductAccessCard` per entitled product — product icon, name, plan, switch, `SourceBadge` (*From role* grey / *Custom* primary), seat indicator "Seats 12/15" when seats apply. Products the organization does not subscribe to are not listed. Empty: "Your organization has no active subscriptions."
- **Section "Feature permissions":** collapsible cards per area (Members & access, Subscriptions, Orders, Billing, Security & sign-in, Organization settings), header counter "3 of 5 on". Each `PermissionToggleRow`: name (bold), one-line description, switch, `SourceBadge`, technical name in a tooltip; when disabled, a lock icon and a `ReasonTooltip`.
- Each item with an override has a **Reset to role default** icon button.
- **StickySaveBar:** "{n} unsaved changes", **Cancel**, **Save changes** (primary); **Reset all to role default** (text button with a confirmation dialog). Closing with unsaved changes asks first.
- Changes are staged and saved together; toast "Access updated for {name}". Refusals show the backend message on the item.

## Role defaults tab
- Master–detail: left, the roles with member counts (Organization admin, Member); right, the same Product access and Feature permissions sections as the member panel, editing that role's defaults.
- *Organization admin*: every feature permission locked ON with the reason "Organization admins always have every permission".
- **Save** first shows the impact: "This changes access for {n} members without custom access. Members with custom access keep their overrides." → **Apply** / **Cancel**.

## Activity tab
- Timeline of access changes from the audit log: actor, member, change ("Manage subscriptions: Off → On"), basis (override, reset, role default, role change), time. Filters: member, actor, date range. Paged.

## Bulk change dialog
1. Choose **Grant** or **Remove**.
2. Choose a product or a feature permission.
3. Preview: affected members, and skipped members with the reason (no free seat, cannot change own access, administrator, permission you don't hold).
4. Confirm: "Grant Product X to 8 members — 2 skipped: no seats". Toast with the result.

## States
Skeletons while loading; backend messages as-is with **Retry**; toasts for success.

## Accessibility and i18n
Switches are real `role="switch"` controls with visible labels and `aria-checked`; the whole row is clickable with a 40 px minimum target; ON/OFF never rely on colour alone (label and icon); disabled reasons are reachable by keyboard (tooltip on focus); the drawer traps focus; axe tests for every tab, the panel and the dialog. Strings under `access.*` in `en.json` and `es.json`.

## API used
[access-management.md](../../06-api/api-requirements/access-management.md).
