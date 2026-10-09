# Data model — Platform ↔ tool synchronization

[REQ-INT-003](../../02-requirements/FRD/platform-tool-sync/requirement.md), [contract v1](../../09-integrations/platform-tool-contract-v1.md). Migrations `database/migrations/V028__subscription_end_of_day.sql` and `V029__platform_tool_sync.sql`, mirrored in `backend/src/main/resources/db/schema.sql` (Flyway disabled).

## Subscription end time (V028, BR-SUB-010)
`product_subscription.expires_at` holds the end as **23:59:00.000 Asia/Kolkata** of the last day, stored in UTC (`18:29:00` of the same calendar date). V028 moves every existing value to that time **on the same India date**, after copying the old values into `product_subscription_expiry_backup`. It can be applied twice. `database/support/V028_dry_run.sql` lists what would change; `database/support/V028_reverse.sql` restores the copied values (exact for the rows V028 touched). A subscription is in force while `started_at <= now <= expires_at`.

## sync_version (V029)
`sync_version BIGINT NOT NULL DEFAULT 1` on `organization`, `org_node`, `customer`, `organization_member`, `product_subscription`, `organization_product_access`. It is the message `version` of the contract: starts at 1, **goes up by one in the transaction of every change** of that row (JPA callback `ToolSyncListener`, built from the version the row had when it was loaded, so a stale copy saved again never lowers it), and is the tombstone's version for a deleted `org_node`. Changes made by SQL or bulk updates outside JPA do not bump it (reconcile repairs the difference).

## tool_connector
A tool the platform is connected to. One row per tool.
| Attribute | Required | Description |
|---|---|---|
| id | Yes | Primary key |
| product_id | Yes | The catalog product (unique) |
| product_code | Yes | The code in messages and in the tool's own configuration, for example `thittam` (unique) |
| base_mcp_url | Yes | The tool's MCP endpoint |
| client_id | Yes | The tool's Keycloak service client: the `azp` the platform accepts from it |
| contract_version | Yes | `1` |
| status | Yes | `ACTIVE`, or `PAUSED` (changes are kept as waiting messages, nothing is sent) |

## tenant_app_schema
Where an organization's data lives in one tool. Unique per (organization, product).
| Attribute | Description |
|---|---|
| organization_id, product_id | The pair |
| tenant_ref | The organization id as text: the `tenantRef` of every message |
| datasource_ref | A reference the **tool** resolves in its own configuration; never a URL or database name |
| schema_name | Reported by the tool, informational |
| status | `PENDING` (provisioning requested), `READY`, `FAILED` |
| schema_version | The version the tool reported |
| last_synced_version, last_synced_at | The newest delivered `tool_delivery.id` for this tenant (a watermark) and when |
| last_error | First 1000 characters of why provisioning failed |

## tool_delivery
One message to one tool for one organization. It names **what** to send (aggregate type and id); the content is built from the current state when it is sent, so a retry or replay never sends something old.
| Attribute | Description |
|---|---|
| event_id | The platform event it came from (`outbox_event.event_id`), or a fresh id for a resync, replay or provisioning request. Unique with the connector and organization |
| tool_connector_id, organization_id, tenant_ref | The destination |
| event_type, aggregate_type, aggregate_id | What to send, for example `OrgNodeUpserted`, `OrgNode`, `4812` (a membership: `Membership`, the `sub`; access: `UserAccess`, `<sub>:<productCode>`) |
| status | `PENDING`, `DELIVERED`, `FAILED` |
| attempts, next_attempt_at, last_error | Retry state. A row is claimed (one attempt counted, next try scheduled) before the call |
| aggregate_version, sent_version | The version when queued (the tombstone's for a delete) and the version the tool accepted |
| created_at, delivered_at | Times |

Indexes: (status, next_attempt_at); (tool_connector_id, organization_id, status); unique (event_id, tool_connector_id, organization_id).

## mcp_idempotency
The **first** result of a tool's call to the platform, answered again for a repeat with the same idempotency key (kept at least 7 days, purged daily).
| Attribute | Description |
|---|---|
| idempotency_key | SHA-256 of `<calling client id>:<key>`, so one tool cannot read another's results |
| tool | The MCP tool name (key part) |
| result_json | The result object |
| created_at | For the purge |
