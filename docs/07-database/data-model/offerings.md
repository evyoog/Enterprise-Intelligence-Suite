# Data model — Offering management (REQ-CAT-005)

Migration `database/migrations/V027__offerings.sql`, mirrored in `schema.sql`. All additive; the `product` table is not changed.

## offering
id, name (150, unique ignoring case), description (1000), status (`DRAFT`/`ACTIVE`/`RETIRED`), created_at, updated_at.

## offering_product
offering_id → offering (cascade), sort_order, product_id → product. Primary key (offering_id, sort_order). The product reference has no cascade: a product in an offering cannot be deleted (a `ProductUsageGuard` says so first).

## product_eligibility
product_id → product (cascade, primary key), audience (`BOTH`/`INDIVIDUAL`/`ORGANIZATION`). No row means `BOTH`.

## product_compatibility
id, product_id → product (cascade), works_with_product_id → product (cascade); unique (product_id, works_with_product_id); product_id ≠ works_with_product_id. One-directional.

The required product is `product_dependency` (REQ-CAT-001); it is not repeated here.
