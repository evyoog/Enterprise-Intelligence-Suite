# API requirements — Ticket Management

| Method | Path | Purpose | Permission | Success | Errors |
|--------|------|---------|------------|---------|--------|
| POST | `/me/tickets` | Create a ticket (BR-SUP-001) | Authenticated (any customer) | 200 (`SupportTicketDto`) | 400 |
| GET | `/me/tickets` | The caller's own tickets | Authenticated | 200 (`SupportTicketDto[]`) | - |
| GET | `/me/tickets/{id}` | One of the caller's own tickets | Authenticated | 200 (`SupportTicketDto`) | 404 (BR-SUP-002) |
| GET | `/admin/support/tickets` | Every ticket | `MANAGE_SUPPORT_TICKETS` | 200 (`SupportTicketDto[]`) | 403 |
| GET | `/admin/support/tickets/{id}` | One ticket, any status | `MANAGE_SUPPORT_TICKETS` | 200 (`SupportTicketDto`) | 403, 404 |
| PATCH | `/admin/support/tickets/{id}` | Categorize/Prioritize/Assign (BR-SUP-003) | `MANAGE_SUPPORT_TICKETS` | 200 (`SupportTicketDto`) | 400, 403, 404 |
| POST | `/admin/support/tickets/{id}/escalate` | Escalate (BR-SUP-004) | `MANAGE_SUPPORT_TICKETS` | 200 (`SupportTicketDto`) | 400, 403, 404 |
| POST | `/admin/support/tickets/{id}/resolve` | Resolve (BR-SUP-005) | `MANAGE_SUPPORT_TICKETS` | 200 (`SupportTicketDto`) | 400, 403, 404 |
| POST | `/admin/support/tickets/{id}/close` | Close (BR-SUP-006) | `MANAGE_SUPPORT_TICKETS` | 200 (`SupportTicketDto`) | 400, 403, 404 |

## Request / response
```json
// PATCH /admin/support/tickets/12
{ "category": "Billing", "priority": "HIGH", "assignedToCustomerId": 41 }
```
```json
// 200
{
  "id": 12,
  "requestedByCustomerId": 5,
  "requestedByName": "Jane Customer",
  "subject": "Can't log in",
  "description": "MFA code never arrives.",
  "category": "Billing",
  "priority": "HIGH",
  "status": "IN_PROGRESS",
  "assignedToCustomerId": 41,
  "assignedToName": "Support Agent",
  "resolutionNote": null,
  "resolvedAt": null,
  "closedAt": null,
  "createdAt": "2027-02-05T09:00:00Z"
}
```

OpenAPI contract: not maintained separately — `SupportTicketController`/`AdminSupportTicketController`/`SupportTicketDto` and this file are the source of truth ([C14](../../../01-business/roadmap/open-decisions.md#c14)).
