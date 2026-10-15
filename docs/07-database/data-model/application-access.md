# Data model — Application access requests

[REQ-TEN-010](../../02-requirements/FRD/application-access/requirement.md). Migration: [`database/migrations/V043__access_requests_order_reminders.sql`](../../../database/migrations/V043__access_requests_order_reminders.sql) (built 2026-10-15, [C103](../../01-business/roadmap/open-decisions.md#c103)), apply by hand after V042. Mirrored in `backend/src/main/resources/db/schema.sql`.

## access_request
A member's self-service request for personal access to a product their organization already subscribes to. Separate from `orders` (asks the organization to buy a subscription, [order-lifecycle data model](cart-checkout.md)) and from `organization_product_access` (the access itself, [access-management data model](access-management.md)) — approving a row here creates/activates an `organization_product_access` row through the same path an administrator's direct grant uses.

| Attribute | Required | Description |
|---|---|---|
| id | Yes | Primary key (`BIGSERIAL`) |
| organization_member_id | Yes | FK `organization_member`, `ON DELETE CASCADE` |
| product_id | Yes | FK `products` |
| status | Yes | `PENDING` \| `APPROVED` \| `REJECTED` \| `CANCELLED`, default `PENDING`, DB `CHECK` constraint |
| message | No | VARCHAR(500), the requester's optional note |
| decided_by_customer_id | No | Who approved or rejected it |
| decided_at | No | When it was decided |
| decision_note | No | VARCHAR(500), required by the service when rejecting |
| created_at | Yes | Set on insert |
| updated_at | Yes | Set on update |

Indexes: `idx_access_request_member` (`organization_member_id`); **one pending request per member and product** is enforced by the partial unique index `uq_access_request_pending (organization_member_id, product_id) WHERE status = 'PENDING'` (BR-AAC-003).

## orders.reminded_at
`ALTER TABLE orders ADD COLUMN IF NOT EXISTS reminded_at TIMESTAMP` — added by the same migration, documented alongside the `orders`/`order_item` tables in the [order-lifecycle data model](cart-checkout.md#orders-and-order_item-v040). Nullable; set once by `OrderService.remindPendingOrders()` the first time a SUBMITTED order is reminded (BR-ORD-012), so a second run of the reminder job never reminds the same order twice.

## Audit
No new table: changes write to `audit_log` with actions `ACCESS_REQUESTED`, `ACCESS_REQUEST_APPROVED`, `ACCESS_REQUEST_REJECTED`, `ACCESS_SELF_REMOVED` (self-removal, not a new `access_request` row), plus the existing `PRODUCT_ACCESS_GRANTED`/`PRODUCT_ACCESS_REVOKED` written by `AccessSyncService#grantProduct`/`revokeProduct` when a request is approved or a member removes their own access.
