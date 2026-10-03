# API requirements — Seats and quantity (organization scope)

Requirement: [REQ-SUB-003](../../02-requirements/FRD/subscription-seats/requirement.md). Caller needs `MANAGE_ORGANIZATION` in their organization (others: 403); subscriptions of another organization: 404.

| Method | Path | Purpose | Success | Errors |
|---|---|---|---|---|
| GET | `/organization/me/subscriptions` | The organization's subscriptions, each with `quantity` | 200 | 403 |
| GET | `/organization/me/subscriptions/{id}/seats` | Seat summary | 200 | 403, 404 |
| PATCH | `/organization/me/subscriptions/{id}/seats` | Body `{ "quantity": 20 }` (1–100 000). Takes effect immediately | 200 | 400, 403, 404, 409 (`INVALID_STATE`: below seats in use, or not ACTIVE/SUSPENDED) |

```json
// GET /organization/me/subscriptions/{id}/seats
{ "subscriptionId": 42, "productName": "Valam.ai", "quantity": 15, "inUse": 12, "minimum": 12,
  "organizationSeatLimit": 15, "maximum": 100000 }
```
