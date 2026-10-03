# Screen: Platform events

| Field | Value |
|---|---|
| Requirement | [REQ-INT-002.6](../../02-requirements/FRD/event-platform/requirement.md) |
| Route | `/admin/integrations/events` |
| Sidebar | Admin → **Integrations** → Platform events |
| Permissions | `MANAGE_INTEGRATIONS`. Others: hidden; API 403 |
| Built | 2026-10-03 — `frontend/src/pages/admin/AdminPlatformEventsPage.tsx` |

Design: C60 corporate standard (page header with icon and accent, tinted table header), [billing-ui-standards.md](billing-ui-standards.md) for chips, states and toasts.

## Layout
- Header: icon (Radio, cyan), area "Integrations", title "Platform events", subtitle.
- Status tiles: Pending, Delivered, Failed counts for the current filter page (clicking filters by that status).
- Filter bar: Event type (select from `/admin/events/types`), Status (All / Pending / Delivered / Failed), From date, To date, **Clear**.
- Table: Occurred, Event type, Aggregate (`Subscription #42`), Status chip (Pending = info, Delivered = success, Failed = error), Attempts, Next attempt; row action **View**; FAILED rows also **Retry**.
- Pagination: 20 per page.
- Detail dialog: all fields, payload pretty-printed in a monospace block, handler receipts, last error; **Retry** for FAILED.

## States
Loading skeleton; empty "No events match these filters."; errors as-is with **Retry**; toast "Event queued for another attempt." after retry.

## Accessibility and i18n
Table with a caption; status chips have text; dialog labelled by its title; axe test. Strings under `admin.events.*` in `en.json` and `es.json`.
