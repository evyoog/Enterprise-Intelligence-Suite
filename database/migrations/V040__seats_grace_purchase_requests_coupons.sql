-- C100 / sprint 2026.4.3 answers: seats (SE-1), renewal grace period and reminder recipients (RR-1, RR-5), multi-item orders with a reason (CA-1, CA-2),
-- and coupons (PB-1 note). Additive. Mirrors backend/src/main/resources/db/schema.sql (Flyway is disabled; apply by hand).
SET search_path TO eis_platform, public;

-- SE-1 B: a seat decrease is stored here and applied with the next renewal.
ALTER TABLE product_subscription ADD COLUMN IF NOT EXISTS pending_quantity INTEGER;
-- RR-1 B: when a term ends with auto-renew off, the subscription is read-only until this time, then it expires.
ALTER TABLE product_subscription ADD COLUMN IF NOT EXISTS grace_ends_at TIMESTAMP;

-- RR-5: people an organization administrator adds to the renewal reminders, with the channels each one gets.
CREATE TABLE IF NOT EXISTS renewal_reminder_recipient (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL REFERENCES organization(id),
    customer_id BIGINT NOT NULL REFERENCES customer(id),
    email_enabled BOOLEAN NOT NULL DEFAULT true,
    in_app_enabled BOOLEAN NOT NULL DEFAULT true,
    created_by_customer_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uq_renewal_reminder_recipient UNIQUE (organization_id, customer_id)
);

-- CA-1 B / CA-2: an organization order holds several products and the requester's reason.
ALTER TABLE orders ADD COLUMN IF NOT EXISTS request_reason VARCHAR(500);
CREATE TABLE IF NOT EXISTS order_item (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES products(id),
    plan_id BIGINT REFERENCES product_plans(id)
);
CREATE INDEX IF NOT EXISTS idx_order_item_order ON order_item (order_id);
-- Orders made before this migration keep their single product: one item each.
INSERT INTO order_item (order_id, product_id, plan_id)
    SELECT o.id, o.product_id, o.plan_id FROM orders o
    WHERE NOT EXISTS (SELECT 1 FROM order_item i WHERE i.order_id = o.id);

-- PB-1 note: coupons configured by the platform administrator.
CREATE TABLE IF NOT EXISTS coupon (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(40) NOT NULL UNIQUE,
    description VARCHAR(200),
    discount_type VARCHAR(10) NOT NULL CHECK (discount_type IN ('PERCENT', 'FIXED')),
    percent_off NUMERIC(5, 2),
    amount_off BIGINT,
    currency VARCHAR(10),
    expires_at TIMESTAMP NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT true,
    redeemed_count INTEGER NOT NULL DEFAULT 0,
    created_by_customer_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);
ALTER TABLE cart ADD COLUMN IF NOT EXISTS coupon_code VARCHAR(40);
ALTER TABLE invoice ADD COLUMN IF NOT EXISTS discount_amount BIGINT NOT NULL DEFAULT 0;
ALTER TABLE invoice ADD COLUMN IF NOT EXISTS coupon_code VARCHAR(40);
ALTER TABLE invoice_line ADD COLUMN IF NOT EXISTS discount_amount BIGINT NOT NULL DEFAULT 0;
