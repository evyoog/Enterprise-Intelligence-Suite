# Data model — Platform events

[REQ-INT-002](../../02-requirements/FRD/event-platform/requirement.md). Migration `database/migrations/V017__platform_events_api_keys.sql` and `backend/src/main/resources/db/schema.sql` (Flyway disabled).

## outbox_event
| Attribute | Required | Description |
|---|---|---|
| id | Yes | Primary key (dispatch order tie-breaker) |
| event_id | Yes | UUID, unique |
| event_type | Yes | For example `SubscriptionCreated` (max 100) |
| aggregate_type, aggregate_id | Yes | For example `Subscription`, `42` |
| occurred_at | Yes | When the change happened (dispatch order) |
| payload | Yes | JSON text; no secrets or card data (BR-2) |
| status | Yes | PENDING, DELIVERED, FAILED |
| attempts | Yes | Delivery attempts so far (default 0) |
| next_attempt_at | No | When the dispatcher may try again (PENDING only) |
| last_error | No | First 1000 characters of the last failure |
| delivered_at | No | When it became DELIVERED |
| created_at | Yes | Row creation |

Indexes: (status, next_attempt_at); (aggregate_type, aggregate_id, occurred_at); (event_type).

## event_handler_receipt
| Attribute | Required | Description |
|---|---|---|
| id | Yes | Primary key |
| handler_name | Yes | Handler identity |
| event_id | Yes | The event's UUID |
| processed_at | Yes | When the handler finished |

Unique (handler_name, event_id) — BR-4.

## event_platform_setting (C91, migration V031)
One row (`id` = 1), created by the migration and on first use.

| Attribute | Required | Description |
|---|---|---|
| id | Yes | Always 1 |
| retention_days | Yes | How long DELIVERED events are kept; 1 to 3650; default 30 (EV-3) |
| alert_recipients | No | Comma-separated lower-case email addresses, at most 20 (EV-4) |
| alert_in_app, alert_email | Yes | Channels of the failure alert, both on by default |
| updated_by, updated_at | — | Who changed it last (Keycloak subject) and when |

Replay has no table of its own: each replay is one audit entry (`EVENTS_REPLAYED`).
