# UI requirements — Ticket Management

## Screens
| Screen | Route | Roles | Wireframe |
|--------|-------|-------|-----------|
| My tickets | `/support/tickets` | Any authenticated customer | Not specified |
| Support tickets admin | `/admin/support/tickets` (from 2027.1.2, [C101](../../../01-business/roadmap/open-decisions.md#c101): the **Tickets** tab of **Support**, next to the **SLA settings** tab at `/admin/support/tickets?tab=sla`) | `MANAGE_SUPPORT_TICKETS` | Not specified |

## Fields and validation
| Field | Type | Required | Validation | Error message (i18n key) |
|-------|------|----------|------------|--------------------------|
| Subject | Text | Yes | Non-blank | Backend message shown as-is |
| Description | Text (multiline) | Yes | Non-blank | Backend message shown as-is |
| Category (admin) | Text | No | - | - |
| Priority (admin) | Select | No | One of LOW/MEDIUM/HIGH/URGENT | - |
| Assign to (admin) | Customer id | No | - | - |
| Resolution note (admin) | Text | No | - | - |

## States
- Empty: "You have not created any tickets yet." (`support.myTickets.noTickets`)
- Loading: handled per section (form/list load independently)
- Error: the backend's own message shown in a dismissible `Alert`
- Admin per-ticket controls (category/priority/assign/escalate/resolve) are disabled once a ticket is RESOLVED or CLOSED, matching BR-SUP-003/.004/.005; Close only shows while RESOLVED.

## SLA additions (C101)
- Admin ticket rows show a **first-response chip** and a **resolution chip** (state and due time), a breach flag and an **SLA filter**; My tickets shows "We aim to respond by … and resolve by …" and never the SLA state. Details: [sla-management.md](../../../05-ui/screen-requirements/sla-management.md) ([REQ-SUP-002](../sla-management/ui-requirements.md)).

## Accessibility and localization
- All fields are labeled MUI controls (`TextField`/`Select` with `InputLabel`+`labelId`).
- All strings are in `frontend/src/i18n/locales/{en,es}.json` under `support.*`.
