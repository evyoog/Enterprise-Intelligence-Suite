# Data model — Free trials and demo requests

[REQ-MKT-005](../../02-requirements/FRD/product-evaluation/requirement.md). Decision [C102](../../01-business/roadmap/open-decisions.md#c102) (2026-10-15). Migration [`V042__trials_and_demo_requests.sql`](../../../database/migrations/V042__trials_and_demo_requests.sql), also in `backend/src/main/resources/db/schema.sql` (Flyway is disabled: apply by hand, after V041). **Additive and re-runnable** (`ADD COLUMN IF NOT EXISTS`, the check constraint is dropped and added again). Schema `eis_platform`. No new table: a trial is a subscription, and a demo request is a support ticket.

## Columns added
| Table | Column | Description |
|---|---|---|
| products | trial_days | INTEGER, nullable. The length of the product's free trial in days; **null = the product offers no trial** (every existing product). CHECK `chk_products_trial_days`: null, or between 1 and 90 |
| product_subscription | trial | BOOLEAN NOT NULL DEFAULT false. True while the subscription is a free trial: on the product's highest plan, `expires_at` is the end of the trial, `auto_renew` is false, no grace date. **A purchase sets it back to false**; existing rows are false |
| product_subscription | trial_reminded_at | TIMESTAMP, nullable. When the "3 days left" notice of the trial was sent; set once |

A trial uses the columns the subscription already has: `status` (ACTIVE while it runs, EXPIRED when it ends), `plan_id` (the highest-priced plan of the product, null for a product without plans), `started_at`, `expires_at`, `quantity` (1, or the organization's seats), `auto_renew` (false) and `grace_ends_at` (never set). The owner is `owner_customer_id` or `owner_organization_id` as for any subscription.

## What is not stored
| Thing | Where it lives |
|---|---|
| The trial's tenant and database | In the hosted product, created when it receives the `SubscriptionCreated` event; EIS creates none ([REQ-ORD-002](../../02-requirements/FRD/provisioning-contract/requirement.md)) |
| The trial's service instance | The existing `service_instance` row of the subscription ([service-instances-sla-incidents.md](service-instances-sla-incidents.md)); `DEPROVISIONED` when the trial ends |
| A demo request | A `support_ticket` row with `category` = `Demo request` ([REQ-SUP-001](../../02-requirements/FRD/ticket-management/requirement.md)), the SLA deadlines of [REQ-SUP-002](../../02-requirements/FRD/sla-management/requirement.md) |
| "One trial per product, ever" | Derived: any `product_subscription` row of the owner for the product, in any status, blocks a trial. There is no separate trial history table; the unique indexes `idx_subscription_customer_product` and `idx_subscription_org_product` already allow one subscription row per owner and product, and a purchase reuses the trial's row |
| A comparison | Nothing: `GET /products/compare` reads products, plans and billing details and stores nothing |
| Audit | `audit_log` entries `TRIAL_STARTED`, `TRIAL_EXPIRED`, `TICKET_CREATED` |

## Relationships
| From | To | Relationship |
|---|---|---|
| `products.trial_days` | `product_subscription.trial` | A product with trial days can be tried; each trial is one subscription row with `trial` = true |
| `product_subscription` (trial) | `service_instance` | One instance per subscription, as for any subscription |

## Rules the database enforces
`chk_products_trial_days` keeps `trial_days` null or 1 to 90. The remaining rules (one trial per owner and product, the highest plan, no renewal) are enforced by the backend ([business-rules.md](../../02-requirements/FRD/product-evaluation/business-rules.md), BR-EVL-001 to BR-EVL-017).

## Checks after applying
```sql
SELECT column_name FROM information_schema.columns
 WHERE table_schema = 'eis_platform' AND table_name = 'product_subscription' AND column_name IN ('trial', 'trial_reminded_at');   -- 2 rows
SELECT column_name FROM information_schema.columns
 WHERE table_schema = 'eis_platform' AND table_name = 'products' AND column_name = 'trial_days';                                    -- 1 row
```
