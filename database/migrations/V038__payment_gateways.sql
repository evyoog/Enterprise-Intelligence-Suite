-- C98 / REQ-BIL-003: provider-independent payment gateways. Additive. Mirrors backend/src/main/resources/db/schema.sql (Flyway is disabled; apply by hand).
SET search_path TO eis_platform, public;

-- One row per payment provider: its environment, encrypted credentials, activation, default, methods, last test and webhook status.
CREATE TABLE IF NOT EXISTS payment_gateway_config (
    id BIGSERIAL PRIMARY KEY,
    provider_code VARCHAR(30) NOT NULL UNIQUE,
    environment VARCHAR(10) CHECK (environment IN ('TEST', 'LIVE')),
    activated BOOLEAN NOT NULL DEFAULT false,
    default_gateway BOOLEAN NOT NULL DEFAULT false,
    credentials_encrypted TEXT,
    credentials_updated_at TIMESTAMP,
    config_version INTEGER NOT NULL DEFAULT 1,
    enabled_methods VARCHAR(100),
    test_status VARCHAR(30),
    test_message VARCHAR(300),
    tested_at TIMESTAMP,
    tested_config_version INTEGER,
    last_webhook_at TIMESTAMP,
    last_webhook_event_type VARCHAR(50),
    last_webhook_failure_at TIMESTAMP,
    last_webhook_failure_reason VARCHAR(300),
    webhook_failure_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_by_customer_id BIGINT
);

-- Webhook events are idempotent per provider, not globally.
ALTER TABLE payment_webhook_event ADD COLUMN IF NOT EXISTS provider VARCHAR(30) NOT NULL DEFAULT 'RAZORPAY';
ALTER TABLE payment_webhook_event DROP CONSTRAINT IF EXISTS payment_webhook_event_provider_event_id_key;
ALTER TABLE payment_webhook_event ADD CONSTRAINT uq_payment_webhook_event_provider UNIQUE (provider, provider_event_id);

-- Existing installations keep working: Razorpay stays active and default (its credentials stay in the secrets file until an administrator stores new ones).
INSERT INTO payment_gateway_config (provider_code, activated, default_gateway)
    VALUES ('RAZORPAY', true, true)
    ON CONFLICT (provider_code) DO NOTHING;
