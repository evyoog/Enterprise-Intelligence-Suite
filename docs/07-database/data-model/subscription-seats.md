# Data model — Seats and quantity

[REQ-SUB-003](../../02-requirements/FRD/subscription-seats/requirement.md). Migration `database/migrations/V018__seats_auto_renew_reminders.sql`.

| Table | Column | Description |
|---|---|---|
| product_subscription | quantity | INT NOT NULL DEFAULT 1, CHECK 1–100 000. Seats for an organization subscription; always 1 for individual subscriptions. The migration sets existing organization subscriptions to their organization's licensed seats (at least 1) |

Seats in use are not stored: they are the members who have the product ON (named seats per product, [C90](../../01-business/roadmap/open-decisions.md#c90); the first build counted every ACTIVE member). The organization's existing `licensed_seats` stays the organization-wide limit.

## Scheduled plan change columns
[C97](../../01-business/roadmap/open-decisions.md#c97) SC-1 ([REQ-SUB-001.8–.11](../../02-requirements/FRD/subscription-lifecycle/requirement.md)). Migration [`V037__billing_rules_sprint_2026_4_3.sql`](../../../database/migrations/V037__billing_rules_sprint_2026_4_3.sql), also in `backend/src/main/resources/db/schema.sql`. Additive.

| Table | Column | Description |
|---|---|---|
| product_subscription | pending_plan_id | BIGINT, nullable. The plan a downgrade will change to at the next renewal; null when nothing is scheduled. No foreign key (the plan is checked when applied; a plan that no longer exists is dropped without changing the subscription) |
| product_subscription | pending_plan_requested_at | TIMESTAMP, nullable. When the customer scheduled the change |

Both columns are cleared when the change is applied at renewal, withdrawn, or replaced by an immediate change.

## Pending seat decrease ([C100](../../01-business/roadmap/open-decisions.md#c100) SE-1 B)
[REQ-SUB-003.9](../../02-requirements/FRD/subscription-seats/requirement.md). Migration [`V040__seats_grace_purchase_requests_coupons.sql`](../../../database/migrations/V040__seats_grace_purchase_requests_coupons.sql), also in `backend/src/main/resources/db/schema.sql`. Additive.

| Table | Column | Description |
|---|---|---|
| product_subscription | pending_quantity | INTEGER, nullable. A seat decrease that takes effect at the next renewal; null when none is scheduled. Cleared when applied (the new quantity is the larger of this value, the seats in use and 1), withdrawn, or when the quantity is changed again. No check constraint: the service keeps it between 1 and 100 000 |

Per-seat pricing needs no column: an invoice line's `quantity` holds the seats and `unit_amount` the price of one seat ([billing-payments.md](billing-payments.md)).

