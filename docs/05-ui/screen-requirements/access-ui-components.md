# Shared access components

Used by [Access management](access-management.md) and [Roles & permissions](admin-roles-permissions.md) ([REQ-TEN-005](../../02-requirements/FRD/access-management/requirement.md)). Folder (when built): `frontend/src/components/access/`.

| Component | Purpose | Props (summary) | Accessibility |
|---|---|---|---|
| `PermissionToggleRow` | One feature permission: bold name, one-line description, switch, `SourceBadge`, technical code in a tooltip, lock icon and `ReasonTooltip` when disabled, optional Reset button | `name`, `description`, `code`, `checked`, `source`, `disabledReason?`, `onChange`, `onReset?` | `role="switch"`, labelled by the name, `aria-checked`; the whole row toggles; minimum height 40 px; reason reachable by keyboard |
| `ProductAccessCard` | One entitled product: icon, name, plan, switch, `SourceBadge`, seat indicator "Seats 12/15" | `product`, `plan`, `checked`, `source`, `seats?`, `disabledReason?`, `onChange`, `onReset?` | As above; the seat indicator has a text label |
| `SourceBadge` | Where an item comes from: *From role* (grey) or *Custom* (primary) | `source: 'ROLE' \| 'OVERRIDE'` | Text, never colour alone |
| `StickySaveBar` | Sticky footer: unsaved count, Cancel, Save, optional secondary action | `dirtyCount`, `busy`, `onSave`, `onCancel`, `secondary?` | Status text in `role="status"`; generalises the C60 `SaveBar` |
| `ReasonTooltip` | Explains why a control is disabled | `reason`, `children` | Opens on hover and keyboard focus; the reason is also the control's `aria-describedby` |

Switch style: clear ON (primary colour, check icon) and OFF (neutral, no icon) states with a visible label; works in light and dark mode.
