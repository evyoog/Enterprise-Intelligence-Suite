# API requirements — Platform events (module `integration`)

Requirement: [REQ-INT-002](../../02-requirements/FRD/event-platform/requirement.md). Admin endpoints require the platform permission `MANAGE_INTEGRATIONS` (others: 403). Publishing is internal (Java `OutboxService.publish`), never an HTTP endpoint — the workbook's `API-019 POST /v1/events` is not exposed, because external publishing is not specified.

| Method | Path | Purpose | Success | Errors |
|---|---|---|---|---|
| GET | `/admin/events?type=&status=&from=&to=&page=` | List events, newest first, 20 per page. `type` exact, `status` PENDING/DELIVERED/FAILED, `from`/`to` ISO dates (inclusive) on occurred-at | 200 | 400, 403 |
| GET | `/admin/events/types` | Distinct event types seen (for the filter) | 200 | 403 |
| GET | `/admin/events/{id}` | One event with payload and handler receipts | 200 | 403, 404 |
| POST | `/admin/events/{id}/retry` | Retry a FAILED event (BR-7) | 200 | 403, 404, 409 (`INVALID_STATE`: not FAILED) |

| GET | `/admin/events/summary` | `{pending, failed}` counts of all events (banner on the screen) | 200 | 403 |
| GET | `/admin/events/receivers` | Names of the receivers a replay can target | 200 | 403 |
| POST | `/admin/events/replay` | Body `{receiver, from, to, eventType?, preview}` (dates `YYYY-MM-DD`, UTC, inclusive). `preview=true` only counts. Returns `{preview, receiver, matched, replayed, failed, limit, failures[{eventId,eventType,error}]}` (BR-9) | 200 | 400 (missing field, end before start, more than the limit), 403, 404 (unknown receiver) |
| GET | `/admin/events/settings` | `{retentionDays, alertRecipients[], alertInApp, alertEmail, updatedAt}` | 200 | 403 |
| PUT | `/admin/events/settings` | Partial update of the same fields (BR-11, BR-12) | 200 | 400, 403 |

```json
// GET /admin/events/{id}
{
  "id": 17, "eventId": "5b8e…", "eventType": "SubscriptionCreated",
  "aggregateType": "Subscription", "aggregateId": "42",
  "occurredAt": "2026-10-03T09:00:00Z", "status": "DELIVERED", "attempts": 1,
  "nextAttemptAt": null, "lastError": null, "deliveredAt": "2026-10-03T09:00:05Z",
  "payload": "{\"subscriptionId\":42,\"productId\":7,\"status\":\"ACTIVE\"}",
  "receipts": [{ "handler": "provisioning", "processedAt": "2026-10-03T09:00:05Z" }]
}
```
