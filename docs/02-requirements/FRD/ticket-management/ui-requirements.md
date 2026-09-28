# UI requirements — Ticket Management

## Screens
| Screen | Route | Roles | Wireframe |
|--------|-------|-------|-----------|
| My tickets | `/support/tickets` | Any authenticated customer | Not specified |
| Support tickets admin | `/admin/support/tickets` | `MANAGE_SUPPORT_TICKETS` | Not specified |

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

## Accessibility and localization
- All fields are labeled MUI controls (`TextField`/`Select` with `InputLabel`+`labelId`).
- All strings are in `frontend/src/i18n/locales/{en,es}.json` under `support.*`.
