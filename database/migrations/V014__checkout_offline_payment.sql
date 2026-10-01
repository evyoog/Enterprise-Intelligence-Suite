-- C55: checkout payment screen and Pay by invoice (REQ-BIL-001.18-.21).
-- Mirrors backend/src/main/resources/db/schema.sql.

SET search_path TO eis_platform;

ALTER TABLE invoice ADD COLUMN IF NOT EXISTS payment_route VARCHAR(10)
    CHECK (payment_route IN ('ONLINE', 'OFFLINE'));

ALTER TABLE payment ADD COLUMN IF NOT EXISTS offline_method VARCHAR(20)
    CHECK (offline_method IN ('BANK_TRANSFER', 'NEFT_RTGS', 'CHEQUE'));
ALTER TABLE payment ADD COLUMN IF NOT EXISTS offline_reference VARCHAR(100);
ALTER TABLE payment ADD COLUMN IF NOT EXISTS received_on DATE;
ALTER TABLE payment ADD COLUMN IF NOT EXISTS recorded_by_customer_id BIGINT REFERENCES customer(id);
ALTER TABLE payment ADD COLUMN IF NOT EXISTS note VARCHAR(500);

CREATE TABLE IF NOT EXISTS billing_settings (
    id BIGSERIAL PRIMARY KEY,
    offline_account_name VARCHAR(200),
    offline_bank_name VARCHAR(200),
    offline_account_number VARCHAR(34),
    offline_ifsc VARCHAR(11),
    offline_swift_bic VARCHAR(11),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_by_customer_id BIGINT REFERENCES customer(id)
);
