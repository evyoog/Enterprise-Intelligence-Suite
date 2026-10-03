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
