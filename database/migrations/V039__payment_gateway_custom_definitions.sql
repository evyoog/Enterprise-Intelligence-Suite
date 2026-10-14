-- C99 / REQ-BIL-003: payment gateways an administrator adds from the Payment Gateways screen. Additive. Mirrors backend/src/main/resources/db/schema.sql
-- (Flyway is disabled; apply by hand). The definition holds no secret; credential values stay in credentials_encrypted (AES-GCM).
SET search_path TO eis_platform, public;

ALTER TABLE payment_gateway_config ADD COLUMN IF NOT EXISTS custom BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE payment_gateway_config ADD COLUMN IF NOT EXISTS display_name VARCHAR(100);
ALTER TABLE payment_gateway_config ADD COLUMN IF NOT EXISTS definition TEXT;
