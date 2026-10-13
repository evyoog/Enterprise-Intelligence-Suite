-- C96 / REQ-BIL-002: tax calculation in two modes (internal rules engine, external provider), Draft invoices when tax
-- cannot be determined, invoice tax lines, credit notes and financial-year document numbers (C97 BI-2, TX-6).
-- Additive. Mirrors backend/src/main/resources/db/schema.sql (Flyway is disabled; apply by hand).
-- NO tax rate is seeded: an administrator approves rules (or connects a provider) before invoices can be finalized.

SET search_path TO eis_platform, public;

-- One row (id = 1): the engine switch, provider settings (never credentials) and the last validation result.
CREATE TABLE IF NOT EXISTS tax_settings (
    id BIGINT PRIMARY KEY,
    external_engine_enabled BOOLEAN NOT NULL DEFAULT false,
    config_version INT NOT NULL DEFAULT 1,
    allow_self_approval BOOLEAN NOT NULL DEFAULT false,
    provider_code VARCHAR(40),
    provider_company_code VARCHAR(100),
    provider_environment VARCHAR(12) NOT NULL DEFAULT 'SANDBOX' CHECK (provider_environment IN ('SANDBOX', 'PRODUCTION')),
    provider_timeout_seconds INT NOT NULL DEFAULT 10 CHECK (provider_timeout_seconds BETWEEN 2 AND 60),
    provider_max_attempts INT NOT NULL DEFAULT 3 CHECK (provider_max_attempts BETWEEN 1 AND 5),
    validation_status VARCHAR(15) NOT NULL DEFAULT 'NOT_VALIDATED' CHECK (validation_status IN ('NOT_VALIDATED', 'PASSED', 'FAILED')),
    validated_at TIMESTAMP,
    validation_summary VARCHAR(2000),
    provider_countries VARCHAR(2000),
    provider_currencies VARCHAR(500),
    provider_transaction_types VARCHAR(200),
    mode_changed_at TIMESTAMP,
    mode_changed_by VARCHAR(100),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_by VARCHAR(100)
);
INSERT INTO tax_settings (id) VALUES (1) ON CONFLICT (id) DO NOTHING;

-- Product or service tax classification (for example a SAC code); products point at one by code.
CREATE TABLE IF NOT EXISTS tax_class (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(40) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    hsn_sac_code VARCHAR(20),
    description VARCHAR(500),
    enabled BOOLEAN NOT NULL DEFAULT true,
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);
ALTER TABLE products ADD COLUMN IF NOT EXISTS tax_class_code VARCHAR(40);

-- The supplier's tax registrations, sent to an external provider (the business profile's GSTIN is added automatically).
CREATE TABLE IF NOT EXISTS tax_registration (
    id BIGSERIAL PRIMARY KEY,
    country_code VARCHAR(2) NOT NULL,
    state_code VARCHAR(20),
    tax_type VARCHAR(12) NOT NULL CHECK (tax_type IN ('GST', 'VAT', 'SALES_TAX', 'OTHER')),
    registration_number VARCHAR(50) NOT NULL,
    effective_from DATE,
    effective_to DATE,
    enabled BOOLEAN NOT NULL DEFAULT true,
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

-- A tax rule is one version of a "rule family" (rule_key). Only APPROVED + active rows in force are used.
CREATE TABLE IF NOT EXISTS tax_rule (
    id BIGSERIAL PRIMARY KEY,
    rule_key VARCHAR(36) NOT NULL,
    version INT NOT NULL DEFAULT 1,
    name VARCHAR(150) NOT NULL,
    country_code VARCHAR(2) NOT NULL,
    state_code VARCHAR(20),
    jurisdiction VARCHAR(150),
    tax_type VARCHAR(12) NOT NULL CHECK (tax_type IN ('GST', 'VAT', 'SALES_TAX', 'OTHER')),
    product_class_code VARCHAR(40),
    customer_type VARCHAR(12) NOT NULL DEFAULT 'ANY' CHECK (customer_type IN ('ANY', 'INDIVIDUAL', 'BUSINESS')),
    supply_type VARCHAR(14) NOT NULL DEFAULT 'ANY' CHECK (supply_type IN ('ANY', 'INTRA_STATE', 'INTER_STATE', 'CROSS_BORDER')),
    tax_id_condition VARCHAR(12) NOT NULL DEFAULT 'ANY' CHECK (tax_id_condition IN ('ANY', 'PROVIDED', 'NOT_PROVIDED')),
    treatment VARCHAR(14) NOT NULL DEFAULT 'TAXABLE'
        CHECK (treatment IN ('TAXABLE', 'EXEMPT', 'ZERO_RATED', 'NIL_RATED', 'REVERSE_CHARGE', 'OUT_OF_SCOPE')),
    effective_from DATE NOT NULL,
    effective_to DATE,
    approval_status VARCHAR(18) NOT NULL DEFAULT 'DRAFT'
        CHECK (approval_status IN ('DRAFT', 'PENDING_APPROVAL', 'APPROVED', 'REJECTED')),
    active BOOLEAN NOT NULL DEFAULT false,
    notes VARCHAR(500),
    rejection_reason VARCHAR(500),
    created_by VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    last_edited_by VARCHAR(100),
    submitted_at TIMESTAMP,
    approved_by VARCHAR(100),
    approved_at TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uq_tax_rule_version UNIQUE (rule_key, version),
    CONSTRAINT ck_tax_rule_dates CHECK (effective_to IS NULL OR effective_to >= effective_from)
);
CREATE INDEX IF NOT EXISTS idx_tax_rule_lookup ON tax_rule (country_code, approval_status, active);

CREATE TABLE IF NOT EXISTS tax_rule_component (
    id BIGSERIAL PRIMARY KEY,
    rule_id BIGINT NOT NULL REFERENCES tax_rule(id) ON DELETE CASCADE,
    sort_order INT NOT NULL DEFAULT 0,
    code VARCHAR(20) NOT NULL,
    name VARCHAR(100) NOT NULL,
    rate NUMERIC(9,4) NOT NULL CHECK (rate >= 0 AND rate <= 100),
    compound BOOLEAN NOT NULL DEFAULT false,
    CONSTRAINT uq_tax_rule_component UNIQUE (rule_id, code)
);

-- A customer or organization certificate: after approval the calculation returns an explicit Exempt result.
CREATE TABLE IF NOT EXISTS tax_exemption (
    id BIGSERIAL PRIMARY KEY,
    owner_customer_id BIGINT REFERENCES customer(id),
    owner_organization_id BIGINT REFERENCES organization(id),
    country_code VARCHAR(2) NOT NULL,
    reason VARCHAR(200) NOT NULL,
    certificate_number VARCHAR(100),
    effective_from DATE NOT NULL,
    effective_to DATE,
    status VARCHAR(18) NOT NULL DEFAULT 'PENDING_APPROVAL' CHECK (status IN ('PENDING_APPROVAL', 'APPROVED', 'REVOKED')),
    created_by VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    approved_by VARCHAR(100),
    approved_at TIMESTAMP,
    CHECK (num_nonnulls(owner_customer_id, owner_organization_id) = 1),
    CHECK (effective_to IS NULL OR effective_to >= effective_from)
);

-- One row per tax calculation attempt on an invoice or credit note, with the engine and configuration version pinned.
CREATE TABLE IF NOT EXISTS tax_calculation (
    id BIGSERIAL PRIMARY KEY,
    invoice_id BIGINT REFERENCES invoice(id) ON DELETE CASCADE,
    transaction_ref VARCHAR(80) NOT NULL,
    transaction_type VARCHAR(12) NOT NULL DEFAULT 'SALE',
    engine VARCHAR(10) NOT NULL CHECK (engine IN ('INTERNAL', 'EXTERNAL')),
    config_version INT NOT NULL,
    status VARCHAR(12) NOT NULL CHECK (status IN ('CALCULATED', 'BLOCKED')),
    reason_code VARCHAR(40),
    reason_message VARCHAR(500),
    provider_code VARCHAR(40),
    provider_reference VARCHAR(120),
    attempts INT NOT NULL DEFAULT 1,
    currency VARCHAR(10),
    taxable_total BIGINT,
    tax_total BIGINT,
    response_copy TEXT,
    calculated_at TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_tax_calculation_invoice ON tax_calculation (invoice_id);

-- Invoice: Draft status, the engine used, the place of supply and the issuer / customer snapshots printed on the invoice.
ALTER TABLE invoice DROP CONSTRAINT IF EXISTS invoice_status_check;
ALTER TABLE invoice ADD CONSTRAINT invoice_status_check
    CHECK (status IN ('DRAFT', 'OPEN', 'PAID', 'PARTIALLY_REFUNDED', 'REFUNDED', 'VOID'));
ALTER TABLE invoice ADD COLUMN IF NOT EXISTS tax_engine VARCHAR(10);
ALTER TABLE invoice ADD COLUMN IF NOT EXISTS tax_config_version INT;
ALTER TABLE invoice ADD COLUMN IF NOT EXISTS tax_status VARCHAR(12) NOT NULL DEFAULT 'LEGACY';
ALTER TABLE invoice ADD COLUMN IF NOT EXISTS tax_block_code VARCHAR(40);
ALTER TABLE invoice ADD COLUMN IF NOT EXISTS tax_block_reason VARCHAR(500);
ALTER TABLE invoice ADD COLUMN IF NOT EXISTS tax_calculation_id BIGINT;
ALTER TABLE invoice ADD COLUMN IF NOT EXISTS place_of_supply VARCHAR(100);
ALTER TABLE invoice ADD COLUMN IF NOT EXISTS issuer_snapshot VARCHAR(1500);
ALTER TABLE invoice ADD COLUMN IF NOT EXISTS customer_tax_id VARCHAR(50);
ALTER TABLE invoice_line ADD COLUMN IF NOT EXISTS tax_class_code VARCHAR(40);
ALTER TABLE invoice_line ADD COLUMN IF NOT EXISTS hsn_sac_code VARCHAR(20);
ALTER TABLE invoice_line ADD COLUMN IF NOT EXISTS tax_amount BIGINT NOT NULL DEFAULT 0;

-- The tax components of an invoice, copied at calculation time; later rule changes never change them.
CREATE TABLE IF NOT EXISTS invoice_tax_line (
    id BIGSERIAL PRIMARY KEY,
    invoice_id BIGINT NOT NULL REFERENCES invoice(id) ON DELETE CASCADE,
    invoice_line_id BIGINT REFERENCES invoice_line(id) ON DELETE CASCADE,
    sort_order INT NOT NULL DEFAULT 0,
    component_code VARCHAR(30) NOT NULL,
    component_name VARCHAR(100) NOT NULL,
    tax_type VARCHAR(12),
    jurisdiction VARCHAR(150),
    treatment VARCHAR(14) NOT NULL DEFAULT 'TAXABLE',
    rate NUMERIC(9,4),
    taxable_amount BIGINT NOT NULL,
    tax_amount BIGINT NOT NULL,
    rule_id BIGINT,
    rule_version INT,
    note VARCHAR(300)
);
CREATE INDEX IF NOT EXISTS idx_invoice_tax_line_invoice ON invoice_tax_line (invoice_id);

-- Credit notes (TX-6): a refund reverses the original invoice's tax lines in proportion.
CREATE TABLE IF NOT EXISTS credit_note (
    id BIGSERIAL PRIMARY KEY,
    credit_note_number VARCHAR(40) NOT NULL UNIQUE,
    invoice_id BIGINT NOT NULL REFERENCES invoice(id),
    owner_customer_id BIGINT REFERENCES customer(id),
    owner_organization_id BIGINT REFERENCES organization(id),
    currency VARCHAR(10) NOT NULL,
    subtotal BIGINT NOT NULL,
    tax_amount BIGINT NOT NULL DEFAULT 0,
    total BIGINT NOT NULL,
    reason VARCHAR(500),
    tax_engine VARCHAR(10),
    payment_refund_id BIGINT,
    issued_at TIMESTAMP NOT NULL DEFAULT now(),
    issued_by VARCHAR(100)
);
CREATE INDEX IF NOT EXISTS idx_credit_note_invoice ON credit_note (invoice_id);
CREATE TABLE IF NOT EXISTS credit_note_tax_line (
    id BIGSERIAL PRIMARY KEY,
    credit_note_id BIGINT NOT NULL REFERENCES credit_note(id) ON DELETE CASCADE,
    sort_order INT NOT NULL DEFAULT 0,
    component_code VARCHAR(30) NOT NULL,
    component_name VARCHAR(100) NOT NULL,
    tax_type VARCHAR(12),
    jurisdiction VARCHAR(150),
    rate NUMERIC(9,4),
    taxable_amount BIGINT NOT NULL,
    tax_amount BIGINT NOT NULL
);

-- BI-2: numbers like EIS/26-27/000123, a new sequence each financial year (series INV for invoices, CN for credit notes).
CREATE TABLE IF NOT EXISTS document_number_sequence (
    series VARCHAR(10) NOT NULL,
    financial_year VARCHAR(7) NOT NULL,
    next_value BIGINT NOT NULL DEFAULT 1,
    PRIMARY KEY (series, financial_year)
);

-- BI-2: new invoices are numbered with the prefix EIS and due in 15 days. Settings an administrator never changed
-- (prefix INV and 0 days, the C47 defaults) move to the new defaults; numbers already issued never change.
ALTER TABLE billing_settings ALTER COLUMN invoice_prefix SET DEFAULT 'EIS';
ALTER TABLE billing_settings ALTER COLUMN payment_terms_days SET DEFAULT 15;
UPDATE billing_settings SET invoice_prefix = 'EIS', payment_terms_days = 15 WHERE invoice_prefix = 'INV' AND payment_terms_days = 0;
