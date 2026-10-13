-- C97 (sprint 2026.4.3): BI-1 .. BI-9, SC-1. Additive. Mirrors backend/src/main/resources/db/schema.sql (Flyway is disabled; apply by hand).

SET search_path TO eis_platform, public;

-- BI-4: pay by invoice only for organizations the platform administrator enables. Organizations that already chose pay by invoice
-- before this change keep it, so their open invoices stay payable.
ALTER TABLE organization ADD COLUMN IF NOT EXISTS pay_by_invoice_enabled BOOLEAN NOT NULL DEFAULT false;
UPDATE organization SET pay_by_invoice_enabled = true
    WHERE id IN (SELECT owner_organization_id FROM invoice WHERE payment_route = 'OFFLINE' AND owner_organization_id IS NOT NULL);

-- BI-7: the Terms, Privacy and Refund pages are on the evyoog.com website; EIS links to them. BI-8: which payment gateway takes online payments.
ALTER TABLE billing_settings ADD COLUMN IF NOT EXISTS terms_url VARCHAR(500);
ALTER TABLE billing_settings ADD COLUMN IF NOT EXISTS privacy_url VARCHAR(500);
ALTER TABLE billing_settings ADD COLUMN IF NOT EXISTS refund_policy_url VARCHAR(500);
ALTER TABLE billing_settings ADD COLUMN IF NOT EXISTS active_gateway VARCHAR(30) NOT NULL DEFAULT 'RAZORPAY';

-- BI-9: an optional purchase order number on the invoice.
ALTER TABLE invoice ADD COLUMN IF NOT EXISTS purchase_order_number VARCHAR(50);

-- BI-2: payment reminders 3 days before the due date, on the due date and 7 days after; one row per invoice and kind so none is sent twice.
CREATE TABLE IF NOT EXISTS invoice_reminder (
    id BIGSERIAL PRIMARY KEY,
    invoice_id BIGINT NOT NULL REFERENCES invoice(id) ON DELETE CASCADE,
    kind VARCHAR(12) NOT NULL CHECK (kind IN ('BEFORE_DUE', 'ON_DUE', 'AFTER_DUE')),
    sent_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uq_invoice_reminder UNIQUE (invoice_id, kind)
);

-- SC-1: a downgrade is scheduled and takes effect at the next renewal; an upgrade is immediate.
ALTER TABLE product_subscription ADD COLUMN IF NOT EXISTS pending_plan_id BIGINT;
ALTER TABLE product_subscription ADD COLUMN IF NOT EXISTS pending_plan_requested_at TIMESTAMP;
