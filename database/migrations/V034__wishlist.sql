-- REQ-MKT-004 Wishlist (C94, 2026-10-13). Additive. Mirrors backend/src/main/resources/db/schema.sql (Flyway is disabled; apply by hand).
-- A customer's saved products, shown as the heart on the catalog cards and on the Wishlist page.

SET search_path TO eis_platform, public;

CREATE TABLE IF NOT EXISTS wishlist_item (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES customer(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uq_wishlist_customer_product UNIQUE (customer_id, product_id)
);
CREATE INDEX IF NOT EXISTS idx_wishlist_customer ON wishlist_item (customer_id, created_at DESC);
