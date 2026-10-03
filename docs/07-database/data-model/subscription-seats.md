# Data model — Seats and quantity

[REQ-SUB-003](../../02-requirements/FRD/subscription-seats/requirement.md). Migration `database/migrations/V018__seats_auto_renew_reminders.sql`.

| Table | Column | Description |
|---|---|---|
| product_subscription | quantity | INT NOT NULL DEFAULT 1, CHECK 1–100 000. Seats for an organization subscription; always 1 for individual subscriptions. The migration sets existing organization subscriptions to their organization's licensed seats (at least 1) |

Seats in use are not stored: they are the organization's ACTIVE members (pool default). The organization's existing `licensed_seats` stays the organization-wide limit.
