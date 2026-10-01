# Data model — Cart and checkout

For [REQ-MKT-003](../../02-requirements/FRD/cart-checkout/requirement.md). Built 2026-10-01: `database/migrations/V015__cart_checkout.sql` and `backend/src/main/resources/db/schema.sql` (Flyway disabled). No payment data of any kind is stored here ([BR-BIL-001](../../03-business-rules/BR-BIL-001-no-raw-card-data.md)).

## cart
| Attribute | Required | Description |
|---|---|---|
| id | Yes | Primary key |
| customer_id | Yes | Owner (the signed-in user). Unique: one cart per user |
| updated_at | Yes | Last change to the cart or its items. Basis for an expiry rule if one is decided (REQ-MKT-003 Open question 3) |
| last_checkout_kind | No | `INVOICE` or `ORDER`: the last successful checkout (BR-10 idempotency) |
| last_checkout_ref | No | Its invoice ID, or comma-separated order IDs |
| last_checkout_at | No | When it happened; a repeat within 2 minutes returns it |

## cart_item
| Attribute | Required | Description |
|---|---|---|
| id | Yes | Primary key |
| cart_id | Yes | FK → cart; items are deleted with their cart |
| product_id | Yes | FK → products. Unique per cart (`cart_id, product_id`): a product appears once (BR-3) |
| plan_id | Yes | The product's plan (checked in the service, BR-4). No foreign key, so deleting a plan shows the item as `NOT_AVAILABLE` (BR-6 a) instead of blocking the delete |
| unit_price_at_add | Yes | Plan price, in the currency's smallest unit, when the item was added or its new price confirmed (BR-5, BR-7) |
| currency | Yes | Plan currency when added |
| added_at | Yes | When the item was added |

## Not stored
- No quantity, coupon, discount, fee or shipping column (BR-12; D14 not decided; [C50](../../01-business/roadmap/open-decisions.md#c50)).
- No validation status: issues are calculated on demand (BR-6).
- No audit rows for cart changes (BR-11).

## Effect on billing tables
`invoice_line.subscription_id` (new, nullable, FK → product_subscription) names the subscription each line bills, so one invoice can pay a cart of several products (C59 default for REQ-MKT-003 Open question 1). `invoice.subscription_id` stays required and holds the first billed subscription; renewals still bill one subscription per invoice.
