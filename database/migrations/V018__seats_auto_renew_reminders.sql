-- REQ-SUB-003 (C63): seat quantity on organization subscriptions.
-- REQ-SUB-004 (C64): auto-renewal and renewal reminders.
-- Mirrors backend/src/main/resources/db/schema.sql (Flyway is disabled; apply by hand).

SET search_path TO eis_platform;

-- REQ-SUB-003: seats. Existing organization subscriptions start at their
-- organization's licensed seats (at least 1); individual subscriptions stay 1.
ALTER TABLE product_subscription ADD COLUMN IF NOT EXISTS quantity INT NOT NULL DEFAULT 1;
ALTER TABLE product_subscription DROP CONSTRAINT IF EXISTS chk_subscription_quantity;
ALTER TABLE product_subscription ADD CONSTRAINT chk_subscription_quantity CHECK (quantity BETWEEN 1 AND 100000);
UPDATE product_subscription s
   SET quantity = GREATEST(1, LEAST(100000, o.licensed_seats))
  FROM organization o
 WHERE s.owner_type = 'ORGANIZATION' AND s.owner_organization_id = o.id;

-- REQ-SUB-004: auto-renew, on by default (existing subscriptions too).
ALTER TABLE product_subscription ADD COLUMN IF NOT EXISTS auto_renew BOOLEAN NOT NULL DEFAULT TRUE;

-- REQ-SUB-004: platform reminder defaults (proposed defaults — confirm).
ALTER TABLE billing_settings ADD COLUMN IF NOT EXISTS reminder_lead_days INT NOT NULL DEFAULT 7;
ALTER TABLE billing_settings DROP CONSTRAINT IF EXISTS chk_billing_settings_reminder_lead_days;
ALTER TABLE billing_settings ADD CONSTRAINT chk_billing_settings_reminder_lead_days CHECK (reminder_lead_days BETWEEN 1 AND 30);
ALTER TABLE billing_settings ADD COLUMN IF NOT EXISTS reminder_send_time VARCHAR(5) NOT NULL DEFAULT '09:00';
ALTER TABLE billing_settings ADD COLUMN IF NOT EXISTS reminder_time_zone VARCHAR(64) NOT NULL DEFAULT 'Asia/Kolkata';

-- REQ-SUB-004: per-user reminder settings (no row = defaults) and the sent-reminder log.
CREATE TABLE IF NOT EXISTS renewal_reminder_preference (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL UNIQUE REFERENCES customer(id),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    days_before INT CHECK (days_before BETWEEN 1 AND 30),
    send_time VARCHAR(5),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS renewal_reminder_log (
    id BIGSERIAL PRIMARY KEY,
    subscription_id BIGINT NOT NULL REFERENCES product_subscription(id),
    recipient_customer_id BIGINT NOT NULL REFERENCES customer(id),
    local_date DATE NOT NULL,
    renewal_date TIMESTAMP NOT NULL,
    days_before INT NOT NULL,
    sent_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uq_renewal_reminder_log UNIQUE (subscription_id, recipient_customer_id, local_date)
);
CREATE INDEX IF NOT EXISTS idx_renewal_reminder_log_subscription ON renewal_reminder_log (subscription_id);
