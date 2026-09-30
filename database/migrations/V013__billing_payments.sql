-- Sprint 2026.4.3: 08 Billing & Payments (REQ-BIL-001, C46).
-- Mirrors backend/src/main/resources/db/schema.sql.

SET search_path TO eis_platform;

CREATE TABLE IF NOT EXISTS billing_details (
    id BIGSERIAL PRIMARY KEY,
    owner_customer_id BIGINT REFERENCES customer(id),
    owner_organization_id BIGINT REFERENCES organization(id),
    billing_name VARCHAR(200) NOT NULL,
    billing_email VARCHAR(255) NOT NULL,
    address_line1 VARCHAR(200) NOT NULL,
    address_line2 VARCHAR(200),
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    postal_code VARCHAR(20) NOT NULL,
    country VARCHAR(100) NOT NULL,
    tax_id VARCHAR(50),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    CHECK (num_nonnulls(owner_customer_id, owner_organization_id) = 1)
);
CREATE UNIQUE INDEX IF NOT EXISTS idx_billing_details_customer ON billing_details (owner_customer_id) WHERE owner_customer_id IS NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS idx_billing_details_organization ON billing_details (owner_organization_id) WHERE owner_organization_id IS NOT NULL;

CREATE TABLE IF NOT EXISTS invoice (
    id BIGSERIAL PRIMARY KEY,
    invoice_number VARCHAR(40) UNIQUE,
    owner_customer_id BIGINT REFERENCES customer(id),
    owner_organization_id BIGINT REFERENCES organization(id),
    subscription_id BIGINT NOT NULL REFERENCES product_subscription(id),
    status VARCHAR(25) NOT NULL DEFAULT 'OPEN'
        CHECK (status IN ('OPEN', 'PAID', 'PARTIALLY_REFUNDED', 'REFUNDED', 'VOID')),
    currency VARCHAR(10) NOT NULL,
    subtotal BIGINT NOT NULL,
    tax_amount BIGINT NOT NULL DEFAULT 0,
    total BIGINT NOT NULL,
    period_start TIMESTAMP,
    period_end TIMESTAMP,
    issued_at TIMESTAMP NOT NULL DEFAULT now(),
    due_at TIMESTAMP,
    bill_to_snapshot VARCHAR(1000),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    CHECK (num_nonnulls(owner_customer_id, owner_organization_id) = 1)
);
CREATE INDEX IF NOT EXISTS idx_invoice_customer_status ON invoice (owner_customer_id, status);
CREATE INDEX IF NOT EXISTS idx_invoice_organization_status ON invoice (owner_organization_id, status);

CREATE TABLE IF NOT EXISTS invoice_line (
    id BIGSERIAL PRIMARY KEY,
    invoice_id BIGINT NOT NULL REFERENCES invoice(id) ON DELETE CASCADE,
    description VARCHAR(255) NOT NULL,
    period_start TIMESTAMP,
    period_end TIMESTAMP,
    quantity INT NOT NULL DEFAULT 1,
    unit_amount BIGINT NOT NULL,
    amount BIGINT NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_invoice_line_invoice ON invoice_line (invoice_id);

CREATE TABLE IF NOT EXISTS payment (
    id BIGSERIAL PRIMARY KEY,
    invoice_id BIGINT NOT NULL REFERENCES invoice(id),
    provider VARCHAR(20) NOT NULL DEFAULT 'RAZORPAY',
    provider_order_id VARCHAR(100),
    provider_payment_id VARCHAR(100),
    status VARCHAR(25) NOT NULL DEFAULT 'CREATED'
        CHECK (status IN ('CREATED', 'CAPTURED', 'FAILED', 'PARTIALLY_REFUNDED', 'REFUNDED')),
    currency VARCHAR(10) NOT NULL,
    amount BIGINT NOT NULL,
    refunded_amount BIGINT NOT NULL DEFAULT 0,
    method_type VARCHAR(20),
    method_network VARCHAR(40),
    method_last4 VARCHAR(4),
    failure_reason VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    captured_at TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_payment_invoice ON payment (invoice_id);
CREATE UNIQUE INDEX IF NOT EXISTS idx_payment_provider_order ON payment (provider_order_id) WHERE provider_order_id IS NOT NULL;

CREATE TABLE IF NOT EXISTS payment_refund (
    id BIGSERIAL PRIMARY KEY,
    payment_id BIGINT NOT NULL REFERENCES payment(id),
    provider_refund_id VARCHAR(100),
    amount BIGINT NOT NULL,
    reason VARCHAR(500) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PROCESSED',
    requested_by_customer_id BIGINT NOT NULL REFERENCES customer(id),
    created_at TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_payment_refund_payment ON payment_refund (payment_id);

CREATE TABLE IF NOT EXISTS payment_method (
    id BIGSERIAL PRIMARY KEY,
    owner_customer_id BIGINT REFERENCES customer(id),
    owner_organization_id BIGINT REFERENCES organization(id),
    provider_token_ref VARCHAR(100) NOT NULL,
    type VARCHAR(10) NOT NULL CHECK (type IN ('CARD', 'UPI')),
    network VARCHAR(40),
    last4 VARCHAR(4),
    expiry_month INT,
    expiry_year INT,
    card_type VARCHAR(20),
    issuer VARCHAR(100),
    upi_masked VARCHAR(100),
    is_default BOOLEAN NOT NULL DEFAULT false,
    consent_at TIMESTAMP,
    status VARCHAR(10) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'REMOVED')),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    CHECK (num_nonnulls(owner_customer_id, owner_organization_id) = 1)
);
CREATE INDEX IF NOT EXISTS idx_payment_method_customer_status ON payment_method (owner_customer_id, status);
CREATE INDEX IF NOT EXISTS idx_payment_method_organization_status ON payment_method (owner_organization_id, status);

CREATE TABLE IF NOT EXISTS payment_webhook_event (
    id BIGSERIAL PRIMARY KEY,
    provider_event_id VARCHAR(100) NOT NULL UNIQUE,
    event_type VARCHAR(50) NOT NULL,
    payment_id BIGINT REFERENCES payment(id),
    received_at TIMESTAMP NOT NULL DEFAULT now(),
    processed_at TIMESTAMP,
    payload_summary VARCHAR(500)
);
