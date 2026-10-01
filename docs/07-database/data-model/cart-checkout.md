# Data model — Cart and checkout

Proposed for [REQ-MKT-003](../../02-requirements/FRD/cart-checkout/requirement.md). Final column types are set in the migration (`database/migrations/V0NN__cart.sql` and `backend/src/main/resources/db/schema.sql`, Flyway disabled) when the feature is built. No payment data of any kind is stored here ([BR-BIL-001](../../03-business-rules/BR-BIL-001-no-raw-card-data.md)).

## cart
| Attribute | Required | Description |
|---|---|---|
| id | Yes | Primary key |
| customer_id | Yes | Owner (the signed-in user). Unique: one cart per user |
| updated_at | Yes | Last change to the cart or its items. Basis for an expiry rule if one is decided (REQ-MKT-003 Open question 3) |

## cart_item
| Attribute | Required | Description |
|---|---|---|
| id | Yes | Primary key |
| cart_id | Yes | FK → cart; items are deleted with their cart |
| product_id | Yes | FK → products. Unique per cart (`cart_id, product_id`): a product appears once (BR-3) |
| plan_id | Yes | FK → product_plans; must belong to `product_id` (BR-4) |
| unit_price_at_add | Yes | Plan price, in the currency's smallest unit, when the item was added or its new price confirmed (BR-5, BR-7) |
| currency | Yes | Plan currency when added |
| added_at | Yes | When the item was added |

## Not stored
- No quantity, coupon, discount, fee or shipping column (BR-12; D14 not decided; [C50](../../01-business/roadmap/open-decisions.md#c50)).
- No validation status: issues are calculated on demand (BR-6).
- No audit rows for cart changes (BR-11).

## Effect on billing tables (depends on REQ-MKT-003 Open question 1)
Today `invoice.subscription_id` links one invoice to one subscription ([billing-payments.md](billing-payments.md)). If one invoice may pay a cart with several products, invoice lines need their own subscription reference and `invoice.subscription_id` becomes optional. This change is **not** proposed until Open question 1 is answered.
