-- C60: full billing settings — business profile (invoice issuer), invoicing,
-- more offline payment details, checkout payment methods and Razorpay
-- Checkout appearance. None of these are secrets (BR-SEC-001: Razorpay keys
-- stay in config/secrets.env). Mirrors backend/src/main/resources/db/schema.sql.

SET search_path TO eis_platform;

-- C60: business profile (the invoice issuer)
ALTER TABLE billing_settings ADD COLUMN IF NOT EXISTS business_legal_name VARCHAR(200);
ALTER TABLE billing_settings ADD COLUMN IF NOT EXISTS business_trade_name VARCHAR(200);
ALTER TABLE billing_settings ADD COLUMN IF NOT EXISTS business_gstin VARCHAR(15);
ALTER TABLE billing_settings ADD COLUMN IF NOT EXISTS business_pan VARCHAR(10);
ALTER TABLE billing_settings ADD COLUMN IF NOT EXISTS business_cin VARCHAR(21);
ALTER TABLE billing_settings ADD COLUMN IF NOT EXISTS business_address_line1 VARCHAR(200);
ALTER TABLE billing_settings ADD COLUMN IF NOT EXISTS business_address_line2 VARCHAR(200);
ALTER TABLE billing_settings ADD COLUMN IF NOT EXISTS business_city VARCHAR(100);
ALTER TABLE billing_settings ADD COLUMN IF NOT EXISTS business_state VARCHAR(100);
ALTER TABLE billing_settings ADD COLUMN IF NOT EXISTS business_postal_code VARCHAR(20);
ALTER TABLE billing_settings ADD COLUMN IF NOT EXISTS business_country VARCHAR(100);
ALTER TABLE billing_settings ADD COLUMN IF NOT EXISTS business_email VARCHAR(255);
ALTER TABLE billing_settings ADD COLUMN IF NOT EXISTS business_phone VARCHAR(30);
ALTER TABLE billing_settings ADD COLUMN IF NOT EXISTS business_website VARCHAR(255);
-- C60: invoicing
ALTER TABLE billing_settings ADD COLUMN IF NOT EXISTS invoice_prefix VARCHAR(10) NOT NULL DEFAULT 'INV';
ALTER TABLE billing_settings ADD COLUMN IF NOT EXISTS payment_terms_days INT NOT NULL DEFAULT 0 CHECK (payment_terms_days BETWEEN 0 AND 365);
ALTER TABLE billing_settings ADD COLUMN IF NOT EXISTS invoice_footer_note VARCHAR(500);
-- C60: more offline payment details
ALTER TABLE billing_settings ADD COLUMN IF NOT EXISTS offline_branch_name VARCHAR(200);
ALTER TABLE billing_settings ADD COLUMN IF NOT EXISTS offline_account_type VARCHAR(10) CHECK (offline_account_type IN ('CURRENT', 'SAVINGS'));
ALTER TABLE billing_settings ADD COLUMN IF NOT EXISTS offline_iban VARCHAR(34);
ALTER TABLE billing_settings ADD COLUMN IF NOT EXISTS offline_micr VARCHAR(9);
ALTER TABLE billing_settings ADD COLUMN IF NOT EXISTS offline_upi_id VARCHAR(100);
ALTER TABLE billing_settings ADD COLUMN IF NOT EXISTS offline_cheque_payable_to VARCHAR(200);
ALTER TABLE billing_settings ADD COLUMN IF NOT EXISTS offline_cheque_address VARCHAR(500);
ALTER TABLE billing_settings ADD COLUMN IF NOT EXISTS offline_instructions VARCHAR(1000);
ALTER TABLE billing_settings ADD COLUMN IF NOT EXISTS offline_bank_transfer_enabled BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE billing_settings ADD COLUMN IF NOT EXISTS offline_neft_rtgs_enabled BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE billing_settings ADD COLUMN IF NOT EXISTS offline_cheque_enabled BOOLEAN NOT NULL DEFAULT TRUE;
-- C60: checkout payment methods and Razorpay Checkout appearance (not secrets)
ALTER TABLE billing_settings ADD COLUMN IF NOT EXISTS method_card_enabled BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE billing_settings ADD COLUMN IF NOT EXISTS method_upi_enabled BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE billing_settings ADD COLUMN IF NOT EXISTS method_netbanking_enabled BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE billing_settings ADD COLUMN IF NOT EXISTS method_wallet_enabled BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE billing_settings ADD COLUMN IF NOT EXISTS method_pay_by_invoice_enabled BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE billing_settings ADD COLUMN IF NOT EXISTS checkout_display_name VARCHAR(100);
ALTER TABLE billing_settings ADD COLUMN IF NOT EXISTS checkout_description VARCHAR(255);
ALTER TABLE billing_settings ADD COLUMN IF NOT EXISTS checkout_theme_color VARCHAR(7);
