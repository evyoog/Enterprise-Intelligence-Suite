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
| coupon_code | No | The coupon code entered in the cart, in capitals, up to 40 characters ([C100](../../01-business/roadmap/open-decisions.md#c100) PB-1 note, [BR-18](../../02-requirements/FRD/cart-checkout/business-rules.md)). One per cart. Cleared by Remove coupon and by a successful checkout; **not** cleared by Clear cart (open point). V040 |

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
- No quantity, fee or shipping column (BR-12). Seats are computed from the organization's subscription when the cart is shown ([C100](../../01-business/roadmap/open-decisions.md#c100)). The discount is not stored either: it is computed from `cart.coupon_code` each time.
- No validation status: issues are calculated on demand (BR-6).
- No audit rows for cart changes (BR-11).

## Effect on billing tables
`invoice_line.subscription_id` (new, nullable, FK → product_subscription) names the subscription each line bills, so one invoice can pay a cart of several products (C59 default for REQ-MKT-003 Open question 1). `invoice.subscription_id` stays required and holds the first billed subscription; renewals still bill one subscription per invoice.

## orders and order_item (V040)
[C100](../../01-business/roadmap/open-decisions.md#c100) CA-1 B, CA-2; [REQ-ORD-001.8 to .11](../../02-requirements/FRD/order-lifecycle/requirement.md). The `orders` table is from [V009](../../../database/migrations/V009__order_lifecycle_knowledge_base.sql); migration [`V040__seats_grace_purchase_requests_coupons.sql`](../../../database/migrations/V040__seats_grace_purchase_requests_coupons.sql) (also in `backend/src/main/resources/db/schema.sql`) makes an order hold several products and a reason. Additive.

| Table | Column | Description |
|---|---|---|
| orders | request_reason | VARCHAR(500), nullable. The requester's reason for a purchase request. `orders.product_id` and `plan_id` stay and hold the **first** item |

### order_item
One row per product of an order.

| Column | Type | Notes |
|---|---|---|
| id | BIGSERIAL PK | |
| order_id | BIGINT NOT NULL | FK orders, `ON DELETE CASCADE`; index `idx_order_item_order` |
| product_id | BIGINT NOT NULL | FK products. A product appears once per order (checked in the service) |
| plan_id | BIGINT NULL | FK product_plans; null = the product's flat price |

The migration inserts one item for every order that had none, so orders made before V040 keep their single product. A direct purchase ([REQ-MKT-003.13](../../02-requirements/FRD/cart-checkout/requirement.md)) is an `orders` row with status APPROVED, `decided_by_customer_id` the buyer and `decision_note` "Purchased directly".

