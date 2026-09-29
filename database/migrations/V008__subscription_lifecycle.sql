-- Sprint 2026.4.3: 07.01 Subscription Lifecycle & Changes (Suspend,
-- Reactivate, Cancel, Renew, Change plan), 07.04 automatic expiry.
-- Mirrors backend/src/main/resources/db/schema.sql. The existing status
-- column already accepts the new SUSPENDED value (VARCHAR(30)) with no
-- length change needed.

SET search_path TO eis_platform;

ALTER TABLE product_subscription
    ADD COLUMN IF NOT EXISTS plan_id BIGINT REFERENCES product_plans(id);
