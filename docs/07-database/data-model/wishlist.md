# Data model — Wishlist

[REQ-MKT-004](../../02-requirements/FRD/wishlist/requirement.md). Migration `database/migrations/V034__wishlist.sql` (also in `backend/src/main/resources/db/schema.sql`).

## wishlist_item
| Attribute | Required | Description |
|---|---|---|
| id | Yes | Primary key |
| customer_id | Yes | FK → customer, deleted with the customer |
| product_id | Yes | FK → products, deleted with the product |
| created_at | Yes | When it was saved; the list is ordered by it, newest first |

Unique on (`customer_id`, `product_id`) (BR-1). Index on (`customer_id`, `created_at` descending).
