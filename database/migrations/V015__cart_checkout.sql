-- C59: cart and checkout (REQ-MKT-003), and invoice lines per subscription
-- so one invoice can pay a cart of several products.
-- Mirrors backend/src/main/resources/db/schema.sql.

SET search_path TO eis_platform;

ALTER TABLE invoice_line ADD COLUMN IF NOT EXISTS subscription_id BIGINT REFERENCES product_subscription(id);

CREATE TABLE IF NOT EXISTS cart (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL UNIQUE REFERENCES customer(id),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    last_checkout_kind VARCHAR(10) CHECK (last_checkout_kind IN ('INVOICE', 'ORDER')),
    last_checkout_ref VARCHAR(500),
    last_checkout_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS cart_item (
    id BIGSERIAL PRIMARY KEY,
    cart_id BIGINT NOT NULL REFERENCES cart(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES products(id),
    -- No FK: a deleted plan must show as NOT_AVAILABLE (BR-6 a), not block the delete.
    plan_id BIGINT NOT NULL,
    unit_price_at_add BIGINT NOT NULL,
    currency VARCHAR(10) NOT NULL,
    added_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uq_cart_item_product UNIQUE (cart_id, product_id)
);
CREATE INDEX IF NOT EXISTS idx_cart_item_cart ON cart_item (cart_id);
