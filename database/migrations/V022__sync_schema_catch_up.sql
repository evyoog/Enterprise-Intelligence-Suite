-- V022: bring an existing eis_platform database up to the current schema.sql.
-- Idempotent: CREATE ... IF NOT EXISTS, ADD COLUMN IF NOT EXISTS, no drops, no data changes.
-- Generated from backend/src/main/resources/db/schema.sql (tables first, then columns of tables
-- that already existed, then indexes and remaining ALTERs). Needs pg_trgm and pgvector.
-- Run: psql -v ON_ERROR_STOP=1 -f V022__sync_schema_catch_up.sql
SET search_path TO eis_platform, public, vyg_requirement; -- pg_trgm lives in vyg_requirement on the shared sandbox RDS
BEGIN;

CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- 1. Missing tables
CREATE TABLE IF NOT EXISTS products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(2000),
    price NUMERIC(12, 2) NOT NULL,
    image_url VARCHAR(500),
    launch_url VARCHAR(500),
    category VARCHAR(255),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    sso_connected BOOLEAN NOT NULL DEFAULT false,
    featured BOOLEAN NOT NULL DEFAULT false,
    accent_color VARCHAR(7) CHECK (accent_color ~ '^#[0-9A-Fa-f]{6}$'),
    feature_tags VARCHAR(1000),
    documentation_url VARCHAR(500),
    support_url VARCHAR(500),
    version INT NOT NULL DEFAULT 1,
    parent_product_id BIGINT REFERENCES products(id),
    variant_label VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS product_dependencies (
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    depends_on_product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    PRIMARY KEY (product_id, depends_on_product_id)
);

CREATE TABLE IF NOT EXISTS product_plans (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    price NUMERIC(12, 2) NOT NULL,
    billing_period VARCHAR(20) NOT NULL,
    sort_order INT,
    currency VARCHAR(10) NOT NULL DEFAULT 'USD',
    usage_limit INT,
    included_features VARCHAR(1000),
    usage_price NUMERIC(12, 4),
    tier_pricing VARCHAR(500),
    overage_charge NUMERIC(12, 4)
);

CREATE TABLE IF NOT EXISTS platforms (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(2000),
    image_url VARCHAR(500),
    primary_color VARCHAR(7) CHECK (primary_color ~ '^#[0-9A-Fa-f]{6}$'),
    status VARCHAR(10) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE')),
    show_in_catalog BOOLEAN NOT NULL DEFAULT true,
    display_order INT NOT NULL DEFAULT 0 CHECK (display_order BETWEEN 0 AND 9999),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS product_platforms (
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    platform_id BIGINT NOT NULL REFERENCES platforms(id) ON DELETE CASCADE,
    PRIMARY KEY (product_id, platform_id)
);

CREATE TABLE IF NOT EXISTS platform_currency (
    code VARCHAR(10) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT true
);

CREATE TABLE IF NOT EXISTS platform_region (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT true
);

CREATE TABLE IF NOT EXISTS platform_feature_flag (
    flag_key VARCHAR(100) PRIMARY KEY,
    enabled BOOLEAN NOT NULL DEFAULT true,
    description VARCHAR(500)
);

CREATE TABLE IF NOT EXISTS sso_bridge_session (
    id VARCHAR(64) PRIMARY KEY,
    keycloak_sub VARCHAR(255) NOT NULL,
    refresh_token TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS password_reset_token (
    id BIGSERIAL PRIMARY KEY,
    token_hash VARCHAR(255) NOT NULL,
    keycloak_sub VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS customer (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    mobile VARCHAR(30),
    country VARCHAR(100),
    company_name VARCHAR(255),
    job_title VARCHAR(150),
    industry VARCHAR(150),
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING_EMAIL_VERIFICATION',
    keycloak_sub VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS customer_mfa (
    customer_id BIGINT PRIMARY KEY REFERENCES customer(id) ON DELETE CASCADE,
    enabled BOOLEAN NOT NULL DEFAULT false,
    encrypted_secret TEXT,
    secret_set_at TIMESTAMP,
    enrolled_at TIMESTAMP,
    last_verified_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS mfa_recovery_code (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES customer(id) ON DELETE CASCADE,
    code_hash VARCHAR(64) NOT NULL,
    used_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS mfa_login_challenge (
    id VARCHAR(64) PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES customer(id) ON DELETE CASCADE,
    keycloak_sub VARCHAR(255) NOT NULL,
    access_token TEXT NOT NULL,
    refresh_token TEXT NOT NULL,
    attempts INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    expires_at TIMESTAMP NOT NULL,
    kind VARCHAR(10) NOT NULL DEFAULT 'VERIFY' CHECK (kind IN ('VERIFY', 'ENROLL')),
    impersonated BOOLEAN NOT NULL DEFAULT false
);

CREATE TABLE IF NOT EXISTS organization (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) NOT NULL,
    type VARCHAR(100),
    industry VARCHAR(150),
    website VARCHAR(500),
    business_email VARCHAR(255) NOT NULL,
    phone VARCHAR(30),
    country VARCHAR(100) NOT NULL,
    state VARCHAR(100),
    city VARCHAR(100),
    address VARCHAR(500),
    gstin VARCHAR(50),
    pan VARCHAR(50),
    company_registration_number VARCHAR(100),
    tax_vat_number VARCHAR(100),
    billing_same_as_address BOOLEAN NOT NULL DEFAULT true,
    billing_address VARCHAR(500),
    billing_country VARCHAR(100),
    billing_state VARCHAR(100),
    billing_city VARCHAR(100),
    parent_organization_id BIGINT REFERENCES organization(id),
    licensed_seats INT NOT NULL DEFAULT 0,
    mfa_required BOOLEAN NOT NULL DEFAULT false,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING_EMAIL_VERIFICATION',
    lifecycle_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
        CHECK (lifecycle_status IN ('ACTIVE', 'SUSPENDED', 'CLOSED')),
    region_id BIGINT REFERENCES platform_region(id),
    allow_seat_overage BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS saml_identity_provider (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL REFERENCES organization(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    entity_id VARCHAR(500) NOT NULL,
    sso_url VARCHAR(500) NOT NULL,
    certificate_pem TEXT NOT NULL,
    metadata_xml TEXT,
    enabled BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS saml_external_identity (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL REFERENCES organization(id) ON DELETE CASCADE,
    idp_entity_id VARCHAR(500) NOT NULL,
    name_id VARCHAR(500) NOT NULL,
    customer_id BIGINT NOT NULL REFERENCES customer(id) ON DELETE CASCADE,
    email VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    last_login_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS saml_login_request (
    id VARCHAR(64) PRIMARY KEY,
    organization_id BIGINT NOT NULL REFERENCES organization(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    expires_at TIMESTAMP NOT NULL,
    consumed_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS organization_member (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL REFERENCES organization(id) ON DELETE CASCADE,
    customer_id BIGINT NOT NULL REFERENCES customer(id),
    org_role VARCHAR(20) NOT NULL DEFAULT 'MEMBER',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    joined_at TIMESTAMP NOT NULL DEFAULT now(),
    deactivated_at TIMESTAMP,
    last_reviewed_at TIMESTAMP,
    last_reviewed_by_customer_id BIGINT REFERENCES customer(id),
    UNIQUE (organization_id, customer_id)
);

CREATE TABLE IF NOT EXISTS organization_group (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL REFERENCES organization(id) ON DELETE CASCADE,
    name VARCHAR(150) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS organization_group_member (
    id BIGSERIAL PRIMARY KEY,
    group_id BIGINT NOT NULL REFERENCES organization_group(id) ON DELETE CASCADE,
    organization_member_id BIGINT NOT NULL REFERENCES organization_member(id) ON DELETE CASCADE,
    added_at TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (group_id, organization_member_id)
);

CREATE TABLE IF NOT EXISTS organization_product_access (
    id BIGSERIAL PRIMARY KEY,
    organization_member_id BIGINT NOT NULL REFERENCES organization_member(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES products(id),
    product_role VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    assigned_at TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (organization_member_id, product_id)
);

CREATE TABLE IF NOT EXISTS product_subscription (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id),
    owner_type VARCHAR(20) NOT NULL,
    owner_customer_id BIGINT REFERENCES customer(id),
    owner_organization_id BIGINT REFERENCES organization(id),
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING_SUBSCRIPTION',
    plan_id BIGINT REFERENCES product_plans(id),
    started_at TIMESTAMP,
    expires_at TIMESTAMP,
    quantity INT NOT NULL DEFAULT 1 CHECK (quantity BETWEEN 1 AND 100000),
    auto_renew BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT chk_subscription_owner CHECK (
        (owner_type = 'INDIVIDUAL' AND owner_customer_id IS NOT NULL AND owner_organization_id IS NULL)
        OR
        (owner_type = 'ORGANIZATION' AND owner_organization_id IS NOT NULL AND owner_customer_id IS NULL)
    )
);

CREATE TABLE IF NOT EXISTS orders (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL REFERENCES organization(id),
    requested_by_customer_id BIGINT NOT NULL REFERENCES customer(id),
    product_id BIGINT NOT NULL REFERENCES products(id),
    plan_id BIGINT REFERENCES product_plans(id),
    status VARCHAR(20) NOT NULL DEFAULT 'SUBMITTED'
        CHECK (status IN ('SUBMITTED', 'APPROVED', 'REJECTED', 'CANCELLED')),
    decided_by_customer_id BIGINT REFERENCES customer(id),
    decided_at TIMESTAMP,
    decision_note VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS email_verification_token (
    id BIGSERIAL PRIMARY KEY,
    token_hash VARCHAR(255) NOT NULL,
    registrant_type VARCHAR(20) NOT NULL,
    customer_id BIGINT REFERENCES customer(id),
    organization_id BIGINT REFERENCES organization(id),
    expires_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT chk_verification_target CHECK (
        (registrant_type = 'INDIVIDUAL' AND customer_id IS NOT NULL AND organization_id IS NULL)
        OR
        (registrant_type = 'ORGANIZATION' AND organization_id IS NOT NULL AND customer_id IS NULL)
    )
);

CREATE TABLE IF NOT EXISTS role (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(60) NOT NULL,
    scope VARCHAR(20) NOT NULL,
    description VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS permission (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(60) NOT NULL,
    description VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS role_permission (
    role_id BIGINT NOT NULL REFERENCES role(id),
    permission_id BIGINT NOT NULL REFERENCES permission(id),
    PRIMARY KEY (role_id, permission_id)
);

CREATE TABLE IF NOT EXISTS privileged_access_request (
    id BIGSERIAL PRIMARY KEY,
    requester_keycloak_sub VARCHAR(255) NOT NULL,
    requester_customer_id BIGINT REFERENCES customer(id),
    scope VARCHAR(20) NOT NULL,
    organization_id BIGINT REFERENCES organization(id),
    permission_name VARCHAR(60) NOT NULL,
    justification VARCHAR(500) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    requested_at TIMESTAMP NOT NULL DEFAULT now(),
    requested_duration_minutes INT NOT NULL,
    decided_at TIMESTAMP,
    decided_by_keycloak_sub VARCHAR(255),
    decision_note VARCHAR(500),
    expires_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS privileged_access_audit_entry (
    id BIGSERIAL PRIMARY KEY,
    request_id BIGINT NOT NULL REFERENCES privileged_access_request(id),
    event_type VARCHAR(20) NOT NULL,
    actor_keycloak_sub VARCHAR(255) NOT NULL,
    occurred_at TIMESTAMP NOT NULL DEFAULT now(),
    note VARCHAR(500)
);

CREATE TABLE IF NOT EXISTS favorite_product (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES customer(id),
    product_id BIGINT NOT NULL REFERENCES products(id),
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS product_usage (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES customer(id),
    product_id BIGINT NOT NULL REFERENCES products(id),
    last_launched_at TIMESTAMP NOT NULL DEFAULT now(),
    launch_count BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS dashboard_preference (
    customer_id BIGINT PRIMARY KEY REFERENCES customer(id),
    widget_order_json TEXT,
    hidden_widgets_json TEXT,
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS customer_preference (
    customer_id BIGINT PRIMARY KEY REFERENCES customer(id),
    language VARCHAR(10),
    region VARCHAR(20),
    time_zone VARCHAR(64),
    theme_mode VARCHAR(10),
    reduced_motion BOOLEAN NOT NULL DEFAULT false,
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS search_history_entry (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES customer(id),
    query VARCHAR(200) NOT NULL,
    searched_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS notification (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES customer(id),
    category VARCHAR(30) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    title VARCHAR(200) NOT NULL,
    message VARCHAR(1000) NOT NULL,
    read BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    read_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS notification_preference (
    customer_id BIGINT PRIMARY KEY REFERENCES customer(id),
    email_disabled_categories_json TEXT,
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS audit_log (
    id BIGSERIAL PRIMARY KEY,
    "timestamp" TIMESTAMP NOT NULL DEFAULT now(),
    action VARCHAR(60) NOT NULL,
    actor_keycloak_sub VARCHAR(255),
    actor_customer_id BIGINT,
    actor_email VARCHAR(255),
    target_type VARCHAR(60),
    target_id VARCHAR(255),
    organization_id BIGINT,
    outcome VARCHAR(20) NOT NULL DEFAULT 'SUCCESS',
    detail VARCHAR(1000)
);

CREATE TABLE IF NOT EXISTS product_service_status (
    product_id BIGINT PRIMARY KEY REFERENCES products(id) ON DELETE CASCADE,
    status VARCHAR(20) NOT NULL DEFAULT 'OPERATIONAL'
        CHECK (status IN ('OPERATIONAL', 'DEGRADED', 'PARTIAL_OUTAGE', 'MAJOR_OUTAGE', 'MAINTENANCE')),
    note VARCHAR(500),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_by_keycloak_sub VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS service_incident (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    title VARCHAR(200) NOT NULL,
    message VARCHAR(4000) NOT NULL,
    started_at TIMESTAMP NOT NULL,
    ended_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    created_by_keycloak_sub VARCHAR(255),
    CHECK (ended_at IS NULL OR ended_at >= started_at)
);

CREATE TABLE IF NOT EXISTS oidc_identity_provider (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL REFERENCES organization(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    issuer_url VARCHAR(500) NOT NULL,
    client_id VARCHAR(255) NOT NULL,
    encrypted_client_secret TEXT NOT NULL,
    scopes VARCHAR(500) NOT NULL DEFAULT 'openid email profile',
    enabled BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS oidc_login_request (
    state VARCHAR(64) PRIMARY KEY,
    organization_id BIGINT NOT NULL REFERENCES organization(id) ON DELETE CASCADE,
    provider_id BIGINT NOT NULL REFERENCES oidc_identity_provider(id) ON DELETE CASCADE,
    nonce VARCHAR(64) NOT NULL,
    code_verifier VARCHAR(128) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    expires_at TIMESTAMP NOT NULL,
    consumed_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS oidc_external_identity (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL REFERENCES organization(id) ON DELETE CASCADE,
    issuer VARCHAR(500) NOT NULL,
    subject VARCHAR(255) NOT NULL,
    customer_id BIGINT NOT NULL REFERENCES customer(id) ON DELETE CASCADE,
    email VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    last_login_at TIMESTAMP,
    UNIQUE (organization_id, issuer, subject)
);

CREATE TABLE IF NOT EXISTS knowledge_article (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    body VARCHAR(20000) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'PUBLISHED')),
    version INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS support_ticket (
    id BIGSERIAL PRIMARY KEY,
    requested_by_customer_id BIGINT NOT NULL REFERENCES customer(id),
    subject VARCHAR(200) NOT NULL,
    description VARCHAR(4000) NOT NULL,
    category VARCHAR(100),
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM' CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'URGENT')),
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN'
        CHECK (status IN ('OPEN', 'IN_PROGRESS', 'ESCALATED', 'RESOLVED', 'CLOSED')),
    assigned_to_customer_id BIGINT REFERENCES customer(id),
    resolution_note VARCHAR(2000),
    resolved_at TIMESTAMP,
    closed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS product_review (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    customer_id BIGINT NOT NULL REFERENCES customer(id),
    rating INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment VARCHAR(2000),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (product_id, customer_id)
);

CREATE TABLE IF NOT EXISTS provider (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    contact_name VARCHAR(255) NOT NULL,
    contact_email VARCHAR(255) NOT NULL,
    description VARCHAR(2000),
    status VARCHAR(20) NOT NULL DEFAULT 'REGISTERED'
        CHECK (status IN ('REGISTERED', 'VERIFIED', 'APPROVED', 'ACTIVE', 'REJECTED')),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS partner_contract (
    id BIGSERIAL PRIMARY KEY,
    provider_id BIGINT NOT NULL UNIQUE REFERENCES provider(id) ON DELETE CASCADE,
    terms VARCHAR(4000) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'EXPIRED')),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

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
    payment_route VARCHAR(10) CHECK (payment_route IN ('ONLINE', 'OFFLINE')),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    CHECK (num_nonnulls(owner_customer_id, owner_organization_id) = 1)
);

CREATE TABLE IF NOT EXISTS invoice_line (
    id BIGSERIAL PRIMARY KEY,
    invoice_id BIGINT NOT NULL REFERENCES invoice(id) ON DELETE CASCADE,
    description VARCHAR(255) NOT NULL,
    period_start TIMESTAMP,
    period_end TIMESTAMP,
    quantity INT NOT NULL DEFAULT 1,
    unit_amount BIGINT NOT NULL,
    amount BIGINT NOT NULL,
    subscription_id BIGINT REFERENCES product_subscription(id)
);

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
    captured_at TIMESTAMP,
    offline_method VARCHAR(20) CHECK (offline_method IN ('BANK_TRANSFER', 'NEFT_RTGS', 'CHEQUE')),
    offline_reference VARCHAR(100),
    received_on DATE,
    recorded_by_customer_id BIGINT REFERENCES customer(id),
    note VARCHAR(500)
);

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

CREATE TABLE IF NOT EXISTS billing_settings (
    id BIGSERIAL PRIMARY KEY,
    offline_account_name VARCHAR(200),
    offline_bank_name VARCHAR(200),
    offline_account_number VARCHAR(34),
    offline_ifsc VARCHAR(11),
    offline_swift_bic VARCHAR(11),
    business_legal_name VARCHAR(200),
    business_trade_name VARCHAR(200),
    business_gstin VARCHAR(15),
    business_pan VARCHAR(10),
    business_cin VARCHAR(21),
    business_address_line1 VARCHAR(200),
    business_address_line2 VARCHAR(200),
    business_city VARCHAR(100),
    business_state VARCHAR(100),
    business_postal_code VARCHAR(20),
    business_country VARCHAR(100),
    business_email VARCHAR(255),
    business_phone VARCHAR(30),
    business_website VARCHAR(255),
    invoice_prefix VARCHAR(10) NOT NULL DEFAULT 'INV',
    payment_terms_days INT NOT NULL DEFAULT 0 CHECK (payment_terms_days BETWEEN 0 AND 365),
    invoice_footer_note VARCHAR(500),
    offline_branch_name VARCHAR(200),
    offline_account_type VARCHAR(10) CHECK (offline_account_type IN ('CURRENT', 'SAVINGS')),
    offline_iban VARCHAR(34),
    offline_micr VARCHAR(9),
    offline_upi_id VARCHAR(100),
    offline_cheque_payable_to VARCHAR(200),
    offline_cheque_address VARCHAR(500),
    offline_instructions VARCHAR(1000),
    offline_bank_transfer_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    offline_neft_rtgs_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    offline_cheque_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    method_card_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    method_upi_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    method_netbanking_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    method_wallet_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    method_pay_by_invoice_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    checkout_display_name VARCHAR(100),
    checkout_description VARCHAR(255),
    checkout_theme_color VARCHAR(7),
    reminder_lead_days INT NOT NULL DEFAULT 7 CHECK (reminder_lead_days BETWEEN 1 AND 30),
    reminder_send_time VARCHAR(5) NOT NULL DEFAULT '09:00',
    reminder_time_zone VARCHAR(64) NOT NULL DEFAULT 'Asia/Kolkata',
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_by_customer_id BIGINT REFERENCES customer(id)
);

CREATE TABLE IF NOT EXISTS payment_webhook_event (
    id BIGSERIAL PRIMARY KEY,
    provider_event_id VARCHAR(100) NOT NULL UNIQUE,
    event_type VARCHAR(50) NOT NULL,
    payment_id BIGINT REFERENCES payment(id),
    received_at TIMESTAMP NOT NULL DEFAULT now(),
    processed_at TIMESTAMP,
    payload_summary VARCHAR(500)
);

CREATE TABLE IF NOT EXISTS cart (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL UNIQUE REFERENCES customer(id),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    last_checkout_kind VARCHAR(10) CHECK (last_checkout_kind IN ('INVOICE', 'ORDER')),
    last_checkout_ref VARCHAR(500),
    last_checkout_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS cart_item (
    id BIGSERIAL PRIMARY KEY,
    cart_id BIGINT NOT NULL REFERENCES cart(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES products(id),
    plan_id BIGINT NOT NULL,
    unit_price_at_add BIGINT NOT NULL,
    currency VARCHAR(10) NOT NULL,
    added_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uq_cart_item_product UNIQUE (cart_id, product_id)
);

CREATE TABLE IF NOT EXISTS outbox_event (
    id BIGSERIAL PRIMARY KEY,
    event_id VARCHAR(36) NOT NULL UNIQUE,
    event_type VARCHAR(100) NOT NULL,
    aggregate_type VARCHAR(100) NOT NULL,
    aggregate_id VARCHAR(100) NOT NULL,
    occurred_at TIMESTAMP NOT NULL,
    payload TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'DELIVERED', 'FAILED')),
    attempts INT NOT NULL DEFAULT 0,
    next_attempt_at TIMESTAMP,
    last_error VARCHAR(1000),
    delivered_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS event_handler_receipt (
    id BIGSERIAL PRIMARY KEY,
    handler_name VARCHAR(100) NOT NULL,
    event_id VARCHAR(36) NOT NULL,
    processed_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uq_event_handler_receipt UNIQUE (handler_name, event_id)
);

CREATE TABLE IF NOT EXISTS api_key (
    id BIGSERIAL PRIMARY KEY,
    owner_customer_id BIGINT NOT NULL REFERENCES customer(id),
    owner_keycloak_sub VARCHAR(255) NOT NULL,
    owner_authorities VARCHAR(1000),
    name VARCHAR(100) NOT NULL,
    key_prefix VARCHAR(20) NOT NULL UNIQUE,
    key_hash VARCHAR(64) NOT NULL UNIQUE,
    expires_at TIMESTAMP,
    revoked_at TIMESTAMP,
    last_used_at TIMESTAMP,
    request_count BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

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

CREATE TABLE IF NOT EXISTS search_document (
    id BIGSERIAL PRIMARY KEY,
    source_type VARCHAR(20) NOT NULL CHECK (source_type IN ('PRODUCT', 'KNOWLEDGE', 'TICKET')),
    source_id BIGINT NOT NULL,
    visibility VARCHAR(10) NOT NULL CHECK (visibility IN ('PUBLIC', 'OWNER')),
    owner_customer_id BIGINT,
    reference VARCHAR(40) NOT NULL,
    title VARCHAR(300) NOT NULL,
    body TEXT,
    keywords VARCHAR(2000),
    title_folded VARCHAR(300) NOT NULL,
    body_folded TEXT,
    keywords_folded VARCHAR(2000),
    content_updated_at TIMESTAMP,
    indexed_at TIMESTAMP NOT NULL DEFAULT now(),
    tsv tsvector GENERATED ALWAYS AS (
        setweight(to_tsvector('simple', coalesce(title_folded, '')), 'A') ||
        setweight(to_tsvector('english', coalesce(title_folded, '')), 'A') ||
        setweight(to_tsvector('spanish', coalesce(title_folded, '')), 'A') ||
        setweight(to_tsvector('simple', coalesce(keywords_folded, '')), 'B') ||
        setweight(to_tsvector('english', coalesce(keywords_folded, '')), 'B') ||
        setweight(to_tsvector('spanish', coalesce(keywords_folded, '')), 'B') ||
        setweight(to_tsvector('simple', coalesce(body_folded, '')), 'D') ||
        setweight(to_tsvector('english', coalesce(body_folded, '')), 'C') ||
        setweight(to_tsvector('spanish', coalesce(body_folded, '')), 'C')
    ) STORED,
    CONSTRAINT uq_search_document_source UNIQUE (source_type, source_id),
    CONSTRAINT chk_search_document_owner CHECK (visibility = 'PUBLIC' OR owner_customer_id IS NOT NULL)
);

CREATE TABLE IF NOT EXISTS search_term (
    term VARCHAR(100) PRIMARY KEY,
    doc_count INT NOT NULL
);

CREATE TABLE IF NOT EXISTS search_synonym (
    id BIGSERIAL PRIMARY KEY,
    terms VARCHAR(500) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS search_query_log (
    id BIGSERIAL PRIMARY KEY,
    query VARCHAR(200) NOT NULL,
    result_count INT NOT NULL,
    mode VARCHAR(10) NOT NULL,
    semantic_used BOOLEAN NOT NULL DEFAULT FALSE,
    took_ms INT NOT NULL,
    searched_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS search_index_run (
    id BIGSERIAL PRIMARY KEY,
    trigger_type VARCHAR(10) NOT NULL CHECK (trigger_type IN ('BACKFILL', 'ADMIN')),
    status VARCHAR(10) NOT NULL CHECK (status IN ('RUNNING', 'DONE', 'FAILED')),
    documents INT NOT NULL DEFAULT 0,
    chunks INT NOT NULL DEFAULT 0,
    embedded INT NOT NULL DEFAULT 0,
    error VARCHAR(1000),
    started_at TIMESTAMP NOT NULL DEFAULT now(),
    finished_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS knowledge_content_version (
    id BIGSERIAL PRIMARY KEY,
    content_id BIGINT NOT NULL REFERENCES knowledge_article(id) ON DELETE CASCADE,
    version_label VARCHAR(10) NOT NULL,
    title VARCHAR(200) NOT NULL,
    short_description VARCHAR(500),
    blocks TEXT,
    type_fields TEXT,
    body VARCHAR(20000) NOT NULL,
    search_text TEXT,
    audience VARCHAR(20) NOT NULL DEFAULT 'PUBLIC',
    audience_org_ids VARCHAR(1000),
    require_product_access BOOLEAN NOT NULL DEFAULT FALSE,
    effective_at TIMESTAMP,
    expires_at TIMESTAMP,
    published_by_sub VARCHAR(100),
    published_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uq_knowledge_content_version UNIQUE (content_id, version_label)
);

CREATE TABLE IF NOT EXISTS knowledge_product (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    slug VARCHAR(120) NOT NULL UNIQUE,
    description VARCHAR(1000),
    catalog_product_id BIGINT REFERENCES products(id) ON DELETE SET NULL,
    display_order INT NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS knowledge_module (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES knowledge_product(id),
    name VARCHAR(100) NOT NULL,
    slug VARCHAR(120) NOT NULL,
    display_order INT NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_knowledge_module_slug UNIQUE (product_id, slug)
);

CREATE TABLE IF NOT EXISTS knowledge_category (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    slug VARCHAR(120) NOT NULL,
    scope VARCHAR(30),
    display_order INT NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_knowledge_category_slug UNIQUE (scope, slug)
);

CREATE TABLE IF NOT EXISTS knowledge_media (
    id BIGSERIAL PRIMARY KEY,
    kind VARCHAR(20) NOT NULL,
    storage_provider VARCHAR(20) NOT NULL,
    s3_bucket VARCHAR(100),
    s3_object_key VARCHAR(500) NOT NULL UNIQUE,
    pending_object_key VARCHAR(500),
    file_name VARCHAR(255) NOT NULL,
    file_size BIGINT NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    etag VARCHAR(100),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING'
        CHECK (status IN ('PENDING', 'READY', 'FAILED', 'CANCELLED', 'DELETED')),
    media_version INT NOT NULL DEFAULT 1,
    product_id BIGINT,
    module_id BIGINT,
    upload_id VARCHAR(300),
    uploaded_by_sub VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    uploaded_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS knowledge_video (
    id BIGSERIAL PRIMARY KEY,
    content_id BIGINT NOT NULL UNIQUE REFERENCES knowledge_article(id) ON DELETE CASCADE,
    video_source_type VARCHAR(20) NOT NULL CHECK (video_source_type IN ('YOUTUBE', 'AWS_S3', 'EXTERNAL_URL')),
    video_url VARCHAR(1000),
    video_id VARCHAR(20),
    media_id BIGINT REFERENCES knowledge_media(id),
    thumbnail_media_id BIGINT REFERENCES knowledge_media(id),
    thumbnail_url VARCHAR(1000),
    duration_seconds INT,
    channel VARCHAR(200),
    transcript TEXT,
    chapters TEXT,
    subtitles TEXT
);

CREATE TABLE IF NOT EXISTS knowledge_feedback (
    id BIGSERIAL PRIMARY KEY,
    content_id BIGINT NOT NULL REFERENCES knowledge_article(id) ON DELETE CASCADE,
    version_label VARCHAR(10),
    customer_id BIGINT,
    voter_sub VARCHAR(100),
    kind VARCHAR(20) NOT NULL CHECK (kind IN ('VOTE', 'OUTDATED', 'SUGGESTION')),
    helpful BOOLEAN,
    reason VARCHAR(40),
    comment_text VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS knowledge_bookmark (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES customer(id),
    content_id BIGINT NOT NULL REFERENCES knowledge_article(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uq_knowledge_bookmark UNIQUE (customer_id, content_id)
);

CREATE TABLE IF NOT EXISTS knowledge_progress (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES customer(id),
    content_id BIGINT NOT NULL REFERENCES knowledge_article(id) ON DELETE CASCADE,
    percent INT NOT NULL DEFAULT 0,
    position_seconds INT,
    completed_at TIMESTAMP,
    last_viewed_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uq_knowledge_progress UNIQUE (customer_id, content_id)
);

CREATE TABLE IF NOT EXISTS knowledge_event (
    id BIGSERIAL PRIMARY KEY,
    event_type VARCHAR(40) NOT NULL,
    content_id BIGINT NOT NULL,
    version_label VARCHAR(10),
    content_type VARCHAR(30),
    product_id BIGINT,
    module_id BIGINT,
    source_type VARCHAR(20),
    organization_id BIGINT,
    percent INT,
    seconds INT,
    viewer_hash VARCHAR(64),
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS knowledge_course (
    id BIGSERIAL PRIMARY KEY,
    content_id BIGINT NOT NULL UNIQUE REFERENCES knowledge_article(id) ON DELETE CASCADE,
    certificate_name VARCHAR(200),
    pass_percent INT
);

CREATE TABLE IF NOT EXISTS knowledge_lesson (
    id BIGSERIAL PRIMARY KEY,
    course_id BIGINT NOT NULL REFERENCES knowledge_course(id) ON DELETE CASCADE,
    lesson_order INT NOT NULL,
    content_id BIGINT REFERENCES knowledge_article(id),
    title VARCHAR(200) NOT NULL
);

-- 2. Missing columns on existing tables
ALTER TABLE products ADD COLUMN IF NOT EXISTS id BIGSERIAL PRIMARY KEY;
ALTER TABLE products ADD COLUMN IF NOT EXISTS name VARCHAR(255) NOT NULL;
ALTER TABLE products ADD COLUMN IF NOT EXISTS description VARCHAR(2000);
ALTER TABLE products ADD COLUMN IF NOT EXISTS price NUMERIC(12, 2) NOT NULL;
ALTER TABLE products ADD COLUMN IF NOT EXISTS image_url VARCHAR(500);
ALTER TABLE products ADD COLUMN IF NOT EXISTS launch_url VARCHAR(500);
ALTER TABLE products ADD COLUMN IF NOT EXISTS category VARCHAR(255);
ALTER TABLE products ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE';
ALTER TABLE products ADD COLUMN IF NOT EXISTS sso_connected BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE products ADD COLUMN IF NOT EXISTS featured BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE products ADD COLUMN IF NOT EXISTS accent_color VARCHAR(7) CHECK (accent_color ~ '^#[0-9A-Fa-f]{6}$');
ALTER TABLE products ADD COLUMN IF NOT EXISTS feature_tags VARCHAR(1000);
ALTER TABLE products ADD COLUMN IF NOT EXISTS documentation_url VARCHAR(500);
ALTER TABLE products ADD COLUMN IF NOT EXISTS support_url VARCHAR(500);
ALTER TABLE products ADD COLUMN IF NOT EXISTS version INT NOT NULL DEFAULT 1;
ALTER TABLE products ADD COLUMN IF NOT EXISTS parent_product_id BIGINT REFERENCES products(id);
ALTER TABLE products ADD COLUMN IF NOT EXISTS variant_label VARCHAR(100);
ALTER TABLE products ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE products ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE product_dependencies ADD COLUMN IF NOT EXISTS product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE;
ALTER TABLE product_dependencies ADD COLUMN IF NOT EXISTS depends_on_product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE;
ALTER TABLE product_plans ADD COLUMN IF NOT EXISTS id BIGSERIAL PRIMARY KEY;
ALTER TABLE product_plans ADD COLUMN IF NOT EXISTS product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE;
ALTER TABLE product_plans ADD COLUMN IF NOT EXISTS name VARCHAR(100) NOT NULL;
ALTER TABLE product_plans ADD COLUMN IF NOT EXISTS price NUMERIC(12, 2) NOT NULL;
ALTER TABLE product_plans ADD COLUMN IF NOT EXISTS billing_period VARCHAR(20) NOT NULL;
ALTER TABLE product_plans ADD COLUMN IF NOT EXISTS sort_order INT;
ALTER TABLE product_plans ADD COLUMN IF NOT EXISTS currency VARCHAR(10) NOT NULL DEFAULT 'USD';
ALTER TABLE product_plans ADD COLUMN IF NOT EXISTS usage_limit INT;
ALTER TABLE product_plans ADD COLUMN IF NOT EXISTS included_features VARCHAR(1000);
ALTER TABLE product_plans ADD COLUMN IF NOT EXISTS usage_price NUMERIC(12, 4);
ALTER TABLE product_plans ADD COLUMN IF NOT EXISTS tier_pricing VARCHAR(500);
ALTER TABLE product_plans ADD COLUMN IF NOT EXISTS overage_charge NUMERIC(12, 4);
ALTER TABLE platforms ADD COLUMN IF NOT EXISTS id BIGSERIAL PRIMARY KEY;
ALTER TABLE platforms ADD COLUMN IF NOT EXISTS name VARCHAR(255) NOT NULL;
ALTER TABLE platforms ADD COLUMN IF NOT EXISTS description VARCHAR(2000);
ALTER TABLE platforms ADD COLUMN IF NOT EXISTS image_url VARCHAR(500);
ALTER TABLE platforms ADD COLUMN IF NOT EXISTS primary_color VARCHAR(7) CHECK (primary_color ~ '^#[0-9A-Fa-f]{6}$');
ALTER TABLE platforms ADD COLUMN IF NOT EXISTS status VARCHAR(10) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE'));
ALTER TABLE platforms ADD COLUMN IF NOT EXISTS show_in_catalog BOOLEAN NOT NULL DEFAULT true;
ALTER TABLE platforms ADD COLUMN IF NOT EXISTS display_order INT NOT NULL DEFAULT 0 CHECK (display_order BETWEEN 0 AND 9999);
ALTER TABLE platforms ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE platforms ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE product_platforms ADD COLUMN IF NOT EXISTS product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE;
ALTER TABLE product_platforms ADD COLUMN IF NOT EXISTS platform_id BIGINT NOT NULL REFERENCES platforms(id) ON DELETE CASCADE;
ALTER TABLE platform_currency ADD COLUMN IF NOT EXISTS code VARCHAR(10) PRIMARY KEY;
ALTER TABLE platform_currency ADD COLUMN IF NOT EXISTS name VARCHAR(100) NOT NULL;
ALTER TABLE platform_currency ADD COLUMN IF NOT EXISTS enabled BOOLEAN NOT NULL DEFAULT true;
ALTER TABLE platform_region ADD COLUMN IF NOT EXISTS id BIGSERIAL PRIMARY KEY;
ALTER TABLE platform_region ADD COLUMN IF NOT EXISTS code VARCHAR(50) NOT NULL UNIQUE;
ALTER TABLE platform_region ADD COLUMN IF NOT EXISTS name VARCHAR(150) NOT NULL;
ALTER TABLE platform_region ADD COLUMN IF NOT EXISTS enabled BOOLEAN NOT NULL DEFAULT true;
ALTER TABLE platform_feature_flag ADD COLUMN IF NOT EXISTS flag_key VARCHAR(100) PRIMARY KEY;
ALTER TABLE platform_feature_flag ADD COLUMN IF NOT EXISTS enabled BOOLEAN NOT NULL DEFAULT true;
ALTER TABLE platform_feature_flag ADD COLUMN IF NOT EXISTS description VARCHAR(500);
ALTER TABLE sso_bridge_session ADD COLUMN IF NOT EXISTS id VARCHAR(64) PRIMARY KEY;
ALTER TABLE sso_bridge_session ADD COLUMN IF NOT EXISTS keycloak_sub VARCHAR(255) NOT NULL;
ALTER TABLE sso_bridge_session ADD COLUMN IF NOT EXISTS refresh_token TEXT NOT NULL;
ALTER TABLE sso_bridge_session ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL;
ALTER TABLE sso_bridge_session ADD COLUMN IF NOT EXISTS expires_at TIMESTAMP NOT NULL;
ALTER TABLE password_reset_token ADD COLUMN IF NOT EXISTS id BIGSERIAL PRIMARY KEY;
ALTER TABLE password_reset_token ADD COLUMN IF NOT EXISTS token_hash VARCHAR(255) NOT NULL;
ALTER TABLE password_reset_token ADD COLUMN IF NOT EXISTS keycloak_sub VARCHAR(255) NOT NULL;
ALTER TABLE password_reset_token ADD COLUMN IF NOT EXISTS email VARCHAR(255) NOT NULL;
ALTER TABLE password_reset_token ADD COLUMN IF NOT EXISTS expires_at TIMESTAMP NOT NULL;
ALTER TABLE password_reset_token ADD COLUMN IF NOT EXISTS used_at TIMESTAMP;
ALTER TABLE password_reset_token ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE customer ADD COLUMN IF NOT EXISTS id BIGSERIAL PRIMARY KEY;
ALTER TABLE customer ADD COLUMN IF NOT EXISTS email VARCHAR(255) NOT NULL;
ALTER TABLE customer ADD COLUMN IF NOT EXISTS first_name VARCHAR(100) NOT NULL;
ALTER TABLE customer ADD COLUMN IF NOT EXISTS last_name VARCHAR(100) NOT NULL;
ALTER TABLE customer ADD COLUMN IF NOT EXISTS mobile VARCHAR(30);
ALTER TABLE customer ADD COLUMN IF NOT EXISTS country VARCHAR(100);
ALTER TABLE customer ADD COLUMN IF NOT EXISTS company_name VARCHAR(255);
ALTER TABLE customer ADD COLUMN IF NOT EXISTS job_title VARCHAR(150);
ALTER TABLE customer ADD COLUMN IF NOT EXISTS industry VARCHAR(150);
ALTER TABLE customer ADD COLUMN IF NOT EXISTS status VARCHAR(30) NOT NULL DEFAULT 'PENDING_EMAIL_VERIFICATION';
ALTER TABLE customer ADD COLUMN IF NOT EXISTS keycloak_sub VARCHAR(255);
ALTER TABLE customer ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE customer ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE customer_mfa ADD COLUMN IF NOT EXISTS customer_id BIGINT PRIMARY KEY REFERENCES customer(id) ON DELETE CASCADE;
ALTER TABLE customer_mfa ADD COLUMN IF NOT EXISTS enabled BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE customer_mfa ADD COLUMN IF NOT EXISTS encrypted_secret TEXT;
ALTER TABLE customer_mfa ADD COLUMN IF NOT EXISTS secret_set_at TIMESTAMP;
ALTER TABLE customer_mfa ADD COLUMN IF NOT EXISTS enrolled_at TIMESTAMP;
ALTER TABLE customer_mfa ADD COLUMN IF NOT EXISTS last_verified_at TIMESTAMP;
ALTER TABLE customer_mfa ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE customer_mfa ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE mfa_recovery_code ADD COLUMN IF NOT EXISTS id BIGSERIAL PRIMARY KEY;
ALTER TABLE mfa_recovery_code ADD COLUMN IF NOT EXISTS customer_id BIGINT NOT NULL REFERENCES customer(id) ON DELETE CASCADE;
ALTER TABLE mfa_recovery_code ADD COLUMN IF NOT EXISTS code_hash VARCHAR(64) NOT NULL;
ALTER TABLE mfa_recovery_code ADD COLUMN IF NOT EXISTS used_at TIMESTAMP;
ALTER TABLE mfa_recovery_code ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE mfa_login_challenge ADD COLUMN IF NOT EXISTS id VARCHAR(64) PRIMARY KEY;
ALTER TABLE mfa_login_challenge ADD COLUMN IF NOT EXISTS customer_id BIGINT NOT NULL REFERENCES customer(id) ON DELETE CASCADE;
ALTER TABLE mfa_login_challenge ADD COLUMN IF NOT EXISTS keycloak_sub VARCHAR(255) NOT NULL;
ALTER TABLE mfa_login_challenge ADD COLUMN IF NOT EXISTS access_token TEXT NOT NULL;
ALTER TABLE mfa_login_challenge ADD COLUMN IF NOT EXISTS refresh_token TEXT NOT NULL;
ALTER TABLE mfa_login_challenge ADD COLUMN IF NOT EXISTS attempts INT NOT NULL DEFAULT 0;
ALTER TABLE mfa_login_challenge ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE mfa_login_challenge ADD COLUMN IF NOT EXISTS expires_at TIMESTAMP NOT NULL;
ALTER TABLE mfa_login_challenge ADD COLUMN IF NOT EXISTS kind VARCHAR(10) NOT NULL DEFAULT 'VERIFY' CHECK (kind IN ('VERIFY', 'ENROLL'));
ALTER TABLE mfa_login_challenge ADD COLUMN IF NOT EXISTS impersonated BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE organization ADD COLUMN IF NOT EXISTS id BIGSERIAL PRIMARY KEY;
ALTER TABLE organization ADD COLUMN IF NOT EXISTS name VARCHAR(255) NOT NULL;
ALTER TABLE organization ADD COLUMN IF NOT EXISTS code VARCHAR(50) NOT NULL;
ALTER TABLE organization ADD COLUMN IF NOT EXISTS type VARCHAR(100);
ALTER TABLE organization ADD COLUMN IF NOT EXISTS industry VARCHAR(150);
ALTER TABLE organization ADD COLUMN IF NOT EXISTS website VARCHAR(500);
ALTER TABLE organization ADD COLUMN IF NOT EXISTS business_email VARCHAR(255) NOT NULL;
ALTER TABLE organization ADD COLUMN IF NOT EXISTS phone VARCHAR(30);
ALTER TABLE organization ADD COLUMN IF NOT EXISTS country VARCHAR(100) NOT NULL;
ALTER TABLE organization ADD COLUMN IF NOT EXISTS state VARCHAR(100);
ALTER TABLE organization ADD COLUMN IF NOT EXISTS city VARCHAR(100);
ALTER TABLE organization ADD COLUMN IF NOT EXISTS address VARCHAR(500);
ALTER TABLE organization ADD COLUMN IF NOT EXISTS gstin VARCHAR(50);
ALTER TABLE organization ADD COLUMN IF NOT EXISTS pan VARCHAR(50);
ALTER TABLE organization ADD COLUMN IF NOT EXISTS company_registration_number VARCHAR(100);
ALTER TABLE organization ADD COLUMN IF NOT EXISTS tax_vat_number VARCHAR(100);
ALTER TABLE organization ADD COLUMN IF NOT EXISTS billing_same_as_address BOOLEAN NOT NULL DEFAULT true;
ALTER TABLE organization ADD COLUMN IF NOT EXISTS billing_address VARCHAR(500);
ALTER TABLE organization ADD COLUMN IF NOT EXISTS billing_country VARCHAR(100);
ALTER TABLE organization ADD COLUMN IF NOT EXISTS billing_state VARCHAR(100);
ALTER TABLE organization ADD COLUMN IF NOT EXISTS billing_city VARCHAR(100);
ALTER TABLE organization ADD COLUMN IF NOT EXISTS parent_organization_id BIGINT REFERENCES organization(id);
ALTER TABLE organization ADD COLUMN IF NOT EXISTS licensed_seats INT NOT NULL DEFAULT 0;
ALTER TABLE organization ADD COLUMN IF NOT EXISTS mfa_required BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE organization ADD COLUMN IF NOT EXISTS status VARCHAR(30) NOT NULL DEFAULT 'PENDING_EMAIL_VERIFICATION';
ALTER TABLE organization ADD COLUMN IF NOT EXISTS lifecycle_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (lifecycle_status IN ('ACTIVE', 'SUSPENDED', 'CLOSED'));
ALTER TABLE organization ADD COLUMN IF NOT EXISTS region_id BIGINT REFERENCES platform_region(id);
ALTER TABLE organization ADD COLUMN IF NOT EXISTS allow_seat_overage BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE organization ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE organization ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE saml_identity_provider ADD COLUMN IF NOT EXISTS id BIGSERIAL PRIMARY KEY;
ALTER TABLE saml_identity_provider ADD COLUMN IF NOT EXISTS organization_id BIGINT NOT NULL REFERENCES organization(id) ON DELETE CASCADE;
ALTER TABLE saml_identity_provider ADD COLUMN IF NOT EXISTS name VARCHAR(255) NOT NULL;
ALTER TABLE saml_identity_provider ADD COLUMN IF NOT EXISTS entity_id VARCHAR(500) NOT NULL;
ALTER TABLE saml_identity_provider ADD COLUMN IF NOT EXISTS sso_url VARCHAR(500) NOT NULL;
ALTER TABLE saml_identity_provider ADD COLUMN IF NOT EXISTS certificate_pem TEXT NOT NULL;
ALTER TABLE saml_identity_provider ADD COLUMN IF NOT EXISTS metadata_xml TEXT;
ALTER TABLE saml_identity_provider ADD COLUMN IF NOT EXISTS enabled BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE saml_identity_provider ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE saml_identity_provider ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE saml_external_identity ADD COLUMN IF NOT EXISTS id BIGSERIAL PRIMARY KEY;
ALTER TABLE saml_external_identity ADD COLUMN IF NOT EXISTS organization_id BIGINT NOT NULL REFERENCES organization(id) ON DELETE CASCADE;
ALTER TABLE saml_external_identity ADD COLUMN IF NOT EXISTS idp_entity_id VARCHAR(500) NOT NULL;
ALTER TABLE saml_external_identity ADD COLUMN IF NOT EXISTS name_id VARCHAR(500) NOT NULL;
ALTER TABLE saml_external_identity ADD COLUMN IF NOT EXISTS customer_id BIGINT NOT NULL REFERENCES customer(id) ON DELETE CASCADE;
ALTER TABLE saml_external_identity ADD COLUMN IF NOT EXISTS email VARCHAR(255);
ALTER TABLE saml_external_identity ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE saml_external_identity ADD COLUMN IF NOT EXISTS last_login_at TIMESTAMP;
ALTER TABLE saml_login_request ADD COLUMN IF NOT EXISTS id VARCHAR(64) PRIMARY KEY;
ALTER TABLE saml_login_request ADD COLUMN IF NOT EXISTS organization_id BIGINT NOT NULL REFERENCES organization(id) ON DELETE CASCADE;
ALTER TABLE saml_login_request ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE saml_login_request ADD COLUMN IF NOT EXISTS expires_at TIMESTAMP NOT NULL;
ALTER TABLE saml_login_request ADD COLUMN IF NOT EXISTS consumed_at TIMESTAMP;
ALTER TABLE organization_member ADD COLUMN IF NOT EXISTS id BIGSERIAL PRIMARY KEY;
ALTER TABLE organization_member ADD COLUMN IF NOT EXISTS organization_id BIGINT NOT NULL REFERENCES organization(id) ON DELETE CASCADE;
ALTER TABLE organization_member ADD COLUMN IF NOT EXISTS customer_id BIGINT NOT NULL REFERENCES customer(id);
ALTER TABLE organization_member ADD COLUMN IF NOT EXISTS org_role VARCHAR(20) NOT NULL DEFAULT 'MEMBER';
ALTER TABLE organization_member ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE';
ALTER TABLE organization_member ADD COLUMN IF NOT EXISTS joined_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE organization_member ADD COLUMN IF NOT EXISTS deactivated_at TIMESTAMP;
ALTER TABLE organization_member ADD COLUMN IF NOT EXISTS last_reviewed_at TIMESTAMP;
ALTER TABLE organization_member ADD COLUMN IF NOT EXISTS last_reviewed_by_customer_id BIGINT REFERENCES customer(id);
ALTER TABLE organization_group ADD COLUMN IF NOT EXISTS id BIGSERIAL PRIMARY KEY;
ALTER TABLE organization_group ADD COLUMN IF NOT EXISTS organization_id BIGINT NOT NULL REFERENCES organization(id) ON DELETE CASCADE;
ALTER TABLE organization_group ADD COLUMN IF NOT EXISTS name VARCHAR(150) NOT NULL;
ALTER TABLE organization_group ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE organization_group_member ADD COLUMN IF NOT EXISTS id BIGSERIAL PRIMARY KEY;
ALTER TABLE organization_group_member ADD COLUMN IF NOT EXISTS group_id BIGINT NOT NULL REFERENCES organization_group(id) ON DELETE CASCADE;
ALTER TABLE organization_group_member ADD COLUMN IF NOT EXISTS organization_member_id BIGINT NOT NULL REFERENCES organization_member(id) ON DELETE CASCADE;
ALTER TABLE organization_group_member ADD COLUMN IF NOT EXISTS added_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE organization_product_access ADD COLUMN IF NOT EXISTS id BIGSERIAL PRIMARY KEY;
ALTER TABLE organization_product_access ADD COLUMN IF NOT EXISTS organization_member_id BIGINT NOT NULL REFERENCES organization_member(id) ON DELETE CASCADE;
ALTER TABLE organization_product_access ADD COLUMN IF NOT EXISTS product_id BIGINT NOT NULL REFERENCES products(id);
ALTER TABLE organization_product_access ADD COLUMN IF NOT EXISTS product_role VARCHAR(50) NOT NULL;
ALTER TABLE organization_product_access ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE';
ALTER TABLE organization_product_access ADD COLUMN IF NOT EXISTS assigned_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE product_subscription ADD COLUMN IF NOT EXISTS id BIGSERIAL PRIMARY KEY;
ALTER TABLE product_subscription ADD COLUMN IF NOT EXISTS product_id BIGINT NOT NULL REFERENCES products(id);
ALTER TABLE product_subscription ADD COLUMN IF NOT EXISTS owner_type VARCHAR(20) NOT NULL;
ALTER TABLE product_subscription ADD COLUMN IF NOT EXISTS owner_customer_id BIGINT REFERENCES customer(id);
ALTER TABLE product_subscription ADD COLUMN IF NOT EXISTS owner_organization_id BIGINT REFERENCES organization(id);
ALTER TABLE product_subscription ADD COLUMN IF NOT EXISTS status VARCHAR(30) NOT NULL DEFAULT 'PENDING_SUBSCRIPTION';
ALTER TABLE product_subscription ADD COLUMN IF NOT EXISTS plan_id BIGINT REFERENCES product_plans(id);
ALTER TABLE product_subscription ADD COLUMN IF NOT EXISTS started_at TIMESTAMP;
ALTER TABLE product_subscription ADD COLUMN IF NOT EXISTS expires_at TIMESTAMP;
ALTER TABLE product_subscription ADD COLUMN IF NOT EXISTS quantity INT NOT NULL DEFAULT 1 CHECK (quantity BETWEEN 1 AND 100000);
ALTER TABLE product_subscription ADD COLUMN IF NOT EXISTS auto_renew BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE product_subscription ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE product_subscription ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE orders ADD COLUMN IF NOT EXISTS id BIGSERIAL PRIMARY KEY;
ALTER TABLE orders ADD COLUMN IF NOT EXISTS organization_id BIGINT NOT NULL REFERENCES organization(id);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS requested_by_customer_id BIGINT NOT NULL REFERENCES customer(id);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS product_id BIGINT NOT NULL REFERENCES products(id);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS plan_id BIGINT REFERENCES product_plans(id);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'SUBMITTED' CHECK (status IN ('SUBMITTED', 'APPROVED', 'REJECTED', 'CANCELLED'));
ALTER TABLE orders ADD COLUMN IF NOT EXISTS decided_by_customer_id BIGINT REFERENCES customer(id);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS decided_at TIMESTAMP;
ALTER TABLE orders ADD COLUMN IF NOT EXISTS decision_note VARCHAR(500);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE orders ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE email_verification_token ADD COLUMN IF NOT EXISTS id BIGSERIAL PRIMARY KEY;
ALTER TABLE email_verification_token ADD COLUMN IF NOT EXISTS token_hash VARCHAR(255) NOT NULL;
ALTER TABLE email_verification_token ADD COLUMN IF NOT EXISTS registrant_type VARCHAR(20) NOT NULL;
ALTER TABLE email_verification_token ADD COLUMN IF NOT EXISTS customer_id BIGINT REFERENCES customer(id);
ALTER TABLE email_verification_token ADD COLUMN IF NOT EXISTS organization_id BIGINT REFERENCES organization(id);
ALTER TABLE email_verification_token ADD COLUMN IF NOT EXISTS expires_at TIMESTAMP NOT NULL;
ALTER TABLE email_verification_token ADD COLUMN IF NOT EXISTS used_at TIMESTAMP;
ALTER TABLE email_verification_token ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE role ADD COLUMN IF NOT EXISTS id BIGSERIAL PRIMARY KEY;
ALTER TABLE role ADD COLUMN IF NOT EXISTS name VARCHAR(60) NOT NULL;
ALTER TABLE role ADD COLUMN IF NOT EXISTS scope VARCHAR(20) NOT NULL;
ALTER TABLE role ADD COLUMN IF NOT EXISTS description VARCHAR(255);
ALTER TABLE permission ADD COLUMN IF NOT EXISTS id BIGSERIAL PRIMARY KEY;
ALTER TABLE permission ADD COLUMN IF NOT EXISTS name VARCHAR(60) NOT NULL;
ALTER TABLE permission ADD COLUMN IF NOT EXISTS description VARCHAR(255);
ALTER TABLE role_permission ADD COLUMN IF NOT EXISTS role_id BIGINT NOT NULL REFERENCES role(id);
ALTER TABLE role_permission ADD COLUMN IF NOT EXISTS permission_id BIGINT NOT NULL REFERENCES permission(id);
ALTER TABLE privileged_access_request ADD COLUMN IF NOT EXISTS id BIGSERIAL PRIMARY KEY;
ALTER TABLE privileged_access_request ADD COLUMN IF NOT EXISTS requester_keycloak_sub VARCHAR(255) NOT NULL;
ALTER TABLE privileged_access_request ADD COLUMN IF NOT EXISTS requester_customer_id BIGINT REFERENCES customer(id);
ALTER TABLE privileged_access_request ADD COLUMN IF NOT EXISTS scope VARCHAR(20) NOT NULL;
ALTER TABLE privileged_access_request ADD COLUMN IF NOT EXISTS organization_id BIGINT REFERENCES organization(id);
ALTER TABLE privileged_access_request ADD COLUMN IF NOT EXISTS permission_name VARCHAR(60) NOT NULL;
ALTER TABLE privileged_access_request ADD COLUMN IF NOT EXISTS justification VARCHAR(500) NOT NULL;
ALTER TABLE privileged_access_request ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'PENDING';
ALTER TABLE privileged_access_request ADD COLUMN IF NOT EXISTS requested_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE privileged_access_request ADD COLUMN IF NOT EXISTS requested_duration_minutes INT NOT NULL;
ALTER TABLE privileged_access_request ADD COLUMN IF NOT EXISTS decided_at TIMESTAMP;
ALTER TABLE privileged_access_request ADD COLUMN IF NOT EXISTS decided_by_keycloak_sub VARCHAR(255);
ALTER TABLE privileged_access_request ADD COLUMN IF NOT EXISTS decision_note VARCHAR(500);
ALTER TABLE privileged_access_request ADD COLUMN IF NOT EXISTS expires_at TIMESTAMP;
ALTER TABLE privileged_access_audit_entry ADD COLUMN IF NOT EXISTS id BIGSERIAL PRIMARY KEY;
ALTER TABLE privileged_access_audit_entry ADD COLUMN IF NOT EXISTS request_id BIGINT NOT NULL REFERENCES privileged_access_request(id);
ALTER TABLE privileged_access_audit_entry ADD COLUMN IF NOT EXISTS event_type VARCHAR(20) NOT NULL;
ALTER TABLE privileged_access_audit_entry ADD COLUMN IF NOT EXISTS actor_keycloak_sub VARCHAR(255) NOT NULL;
ALTER TABLE privileged_access_audit_entry ADD COLUMN IF NOT EXISTS occurred_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE privileged_access_audit_entry ADD COLUMN IF NOT EXISTS note VARCHAR(500);
ALTER TABLE favorite_product ADD COLUMN IF NOT EXISTS id BIGSERIAL PRIMARY KEY;
ALTER TABLE favorite_product ADD COLUMN IF NOT EXISTS customer_id BIGINT NOT NULL REFERENCES customer(id);
ALTER TABLE favorite_product ADD COLUMN IF NOT EXISTS product_id BIGINT NOT NULL REFERENCES products(id);
ALTER TABLE favorite_product ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE product_usage ADD COLUMN IF NOT EXISTS id BIGSERIAL PRIMARY KEY;
ALTER TABLE product_usage ADD COLUMN IF NOT EXISTS customer_id BIGINT NOT NULL REFERENCES customer(id);
ALTER TABLE product_usage ADD COLUMN IF NOT EXISTS product_id BIGINT NOT NULL REFERENCES products(id);
ALTER TABLE product_usage ADD COLUMN IF NOT EXISTS last_launched_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE product_usage ADD COLUMN IF NOT EXISTS launch_count BIGINT NOT NULL DEFAULT 0;
ALTER TABLE dashboard_preference ADD COLUMN IF NOT EXISTS customer_id BIGINT PRIMARY KEY REFERENCES customer(id);
ALTER TABLE dashboard_preference ADD COLUMN IF NOT EXISTS widget_order_json TEXT;
ALTER TABLE dashboard_preference ADD COLUMN IF NOT EXISTS hidden_widgets_json TEXT;
ALTER TABLE dashboard_preference ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE customer_preference ADD COLUMN IF NOT EXISTS customer_id BIGINT PRIMARY KEY REFERENCES customer(id);
ALTER TABLE customer_preference ADD COLUMN IF NOT EXISTS language VARCHAR(10);
ALTER TABLE customer_preference ADD COLUMN IF NOT EXISTS region VARCHAR(20);
ALTER TABLE customer_preference ADD COLUMN IF NOT EXISTS time_zone VARCHAR(64);
ALTER TABLE customer_preference ADD COLUMN IF NOT EXISTS theme_mode VARCHAR(10);
ALTER TABLE customer_preference ADD COLUMN IF NOT EXISTS reduced_motion BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE customer_preference ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE search_history_entry ADD COLUMN IF NOT EXISTS id BIGSERIAL PRIMARY KEY;
ALTER TABLE search_history_entry ADD COLUMN IF NOT EXISTS customer_id BIGINT NOT NULL REFERENCES customer(id);
ALTER TABLE search_history_entry ADD COLUMN IF NOT EXISTS query VARCHAR(200) NOT NULL;
ALTER TABLE search_history_entry ADD COLUMN IF NOT EXISTS searched_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE notification ADD COLUMN IF NOT EXISTS id BIGSERIAL PRIMARY KEY;
ALTER TABLE notification ADD COLUMN IF NOT EXISTS customer_id BIGINT NOT NULL REFERENCES customer(id);
ALTER TABLE notification ADD COLUMN IF NOT EXISTS category VARCHAR(30) NOT NULL;
ALTER TABLE notification ADD COLUMN IF NOT EXISTS severity VARCHAR(20) NOT NULL;
ALTER TABLE notification ADD COLUMN IF NOT EXISTS title VARCHAR(200) NOT NULL;
ALTER TABLE notification ADD COLUMN IF NOT EXISTS message VARCHAR(1000) NOT NULL;
ALTER TABLE notification ADD COLUMN IF NOT EXISTS read BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE notification ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE notification ADD COLUMN IF NOT EXISTS read_at TIMESTAMP;
ALTER TABLE notification_preference ADD COLUMN IF NOT EXISTS customer_id BIGINT PRIMARY KEY REFERENCES customer(id);
ALTER TABLE notification_preference ADD COLUMN IF NOT EXISTS email_disabled_categories_json TEXT;
ALTER TABLE notification_preference ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE audit_log ADD COLUMN IF NOT EXISTS id BIGSERIAL PRIMARY KEY;
ALTER TABLE audit_log ADD COLUMN IF NOT EXISTS "timestamp" TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE audit_log ADD COLUMN IF NOT EXISTS action VARCHAR(60) NOT NULL;
ALTER TABLE audit_log ADD COLUMN IF NOT EXISTS actor_keycloak_sub VARCHAR(255);
ALTER TABLE audit_log ADD COLUMN IF NOT EXISTS actor_customer_id BIGINT;
ALTER TABLE audit_log ADD COLUMN IF NOT EXISTS actor_email VARCHAR(255);
ALTER TABLE audit_log ADD COLUMN IF NOT EXISTS target_type VARCHAR(60);
ALTER TABLE audit_log ADD COLUMN IF NOT EXISTS target_id VARCHAR(255);
ALTER TABLE audit_log ADD COLUMN IF NOT EXISTS organization_id BIGINT;
ALTER TABLE audit_log ADD COLUMN IF NOT EXISTS outcome VARCHAR(20) NOT NULL DEFAULT 'SUCCESS';
ALTER TABLE audit_log ADD COLUMN IF NOT EXISTS detail VARCHAR(1000);
ALTER TABLE product_service_status ADD COLUMN IF NOT EXISTS product_id BIGINT PRIMARY KEY REFERENCES products(id) ON DELETE CASCADE;
ALTER TABLE product_service_status ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'OPERATIONAL' CHECK (status IN ('OPERATIONAL', 'DEGRADED', 'PARTIAL_OUTAGE', 'MAJOR_OUTAGE', 'MAINTENANCE'));
ALTER TABLE product_service_status ADD COLUMN IF NOT EXISTS note VARCHAR(500);
ALTER TABLE product_service_status ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE product_service_status ADD COLUMN IF NOT EXISTS updated_by_keycloak_sub VARCHAR(255);
ALTER TABLE service_incident ADD COLUMN IF NOT EXISTS id BIGSERIAL PRIMARY KEY;
ALTER TABLE service_incident ADD COLUMN IF NOT EXISTS product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE;
ALTER TABLE service_incident ADD COLUMN IF NOT EXISTS title VARCHAR(200) NOT NULL;
ALTER TABLE service_incident ADD COLUMN IF NOT EXISTS message VARCHAR(4000) NOT NULL;
ALTER TABLE service_incident ADD COLUMN IF NOT EXISTS started_at TIMESTAMP NOT NULL;
ALTER TABLE service_incident ADD COLUMN IF NOT EXISTS ended_at TIMESTAMP;
ALTER TABLE service_incident ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE service_incident ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE service_incident ADD COLUMN IF NOT EXISTS created_by_keycloak_sub VARCHAR(255);
ALTER TABLE oidc_identity_provider ADD COLUMN IF NOT EXISTS id BIGSERIAL PRIMARY KEY;
ALTER TABLE oidc_identity_provider ADD COLUMN IF NOT EXISTS organization_id BIGINT NOT NULL REFERENCES organization(id) ON DELETE CASCADE;
ALTER TABLE oidc_identity_provider ADD COLUMN IF NOT EXISTS name VARCHAR(255) NOT NULL;
ALTER TABLE oidc_identity_provider ADD COLUMN IF NOT EXISTS issuer_url VARCHAR(500) NOT NULL;
ALTER TABLE oidc_identity_provider ADD COLUMN IF NOT EXISTS client_id VARCHAR(255) NOT NULL;
ALTER TABLE oidc_identity_provider ADD COLUMN IF NOT EXISTS encrypted_client_secret TEXT NOT NULL;
ALTER TABLE oidc_identity_provider ADD COLUMN IF NOT EXISTS scopes VARCHAR(500) NOT NULL DEFAULT 'openid email profile';
ALTER TABLE oidc_identity_provider ADD COLUMN IF NOT EXISTS enabled BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE oidc_identity_provider ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE oidc_identity_provider ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE oidc_login_request ADD COLUMN IF NOT EXISTS state VARCHAR(64) PRIMARY KEY;
ALTER TABLE oidc_login_request ADD COLUMN IF NOT EXISTS organization_id BIGINT NOT NULL REFERENCES organization(id) ON DELETE CASCADE;
ALTER TABLE oidc_login_request ADD COLUMN IF NOT EXISTS provider_id BIGINT NOT NULL REFERENCES oidc_identity_provider(id) ON DELETE CASCADE;
ALTER TABLE oidc_login_request ADD COLUMN IF NOT EXISTS nonce VARCHAR(64) NOT NULL;
ALTER TABLE oidc_login_request ADD COLUMN IF NOT EXISTS code_verifier VARCHAR(128) NOT NULL;
ALTER TABLE oidc_login_request ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE oidc_login_request ADD COLUMN IF NOT EXISTS expires_at TIMESTAMP NOT NULL;
ALTER TABLE oidc_login_request ADD COLUMN IF NOT EXISTS consumed_at TIMESTAMP;
ALTER TABLE oidc_external_identity ADD COLUMN IF NOT EXISTS id BIGSERIAL PRIMARY KEY;
ALTER TABLE oidc_external_identity ADD COLUMN IF NOT EXISTS organization_id BIGINT NOT NULL REFERENCES organization(id) ON DELETE CASCADE;
ALTER TABLE oidc_external_identity ADD COLUMN IF NOT EXISTS issuer VARCHAR(500) NOT NULL;
ALTER TABLE oidc_external_identity ADD COLUMN IF NOT EXISTS subject VARCHAR(255) NOT NULL;
ALTER TABLE oidc_external_identity ADD COLUMN IF NOT EXISTS customer_id BIGINT NOT NULL REFERENCES customer(id) ON DELETE CASCADE;
ALTER TABLE oidc_external_identity ADD COLUMN IF NOT EXISTS email VARCHAR(255);
ALTER TABLE oidc_external_identity ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE oidc_external_identity ADD COLUMN IF NOT EXISTS last_login_at TIMESTAMP;
ALTER TABLE knowledge_article ADD COLUMN IF NOT EXISTS id BIGSERIAL PRIMARY KEY;
ALTER TABLE knowledge_article ADD COLUMN IF NOT EXISTS title VARCHAR(200) NOT NULL;
ALTER TABLE knowledge_article ADD COLUMN IF NOT EXISTS body VARCHAR(20000) NOT NULL;
ALTER TABLE knowledge_article ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'PUBLISHED'));
ALTER TABLE knowledge_article ADD COLUMN IF NOT EXISTS version INT NOT NULL DEFAULT 1;
ALTER TABLE knowledge_article ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE knowledge_article ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE support_ticket ADD COLUMN IF NOT EXISTS id BIGSERIAL PRIMARY KEY;
ALTER TABLE support_ticket ADD COLUMN IF NOT EXISTS requested_by_customer_id BIGINT NOT NULL REFERENCES customer(id);
ALTER TABLE support_ticket ADD COLUMN IF NOT EXISTS subject VARCHAR(200) NOT NULL;
ALTER TABLE support_ticket ADD COLUMN IF NOT EXISTS description VARCHAR(4000) NOT NULL;
ALTER TABLE support_ticket ADD COLUMN IF NOT EXISTS category VARCHAR(100);
ALTER TABLE support_ticket ADD COLUMN IF NOT EXISTS priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM' CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'URGENT'));
ALTER TABLE support_ticket ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'OPEN' CHECK (status IN ('OPEN', 'IN_PROGRESS', 'ESCALATED', 'RESOLVED', 'CLOSED'));
ALTER TABLE support_ticket ADD COLUMN IF NOT EXISTS assigned_to_customer_id BIGINT REFERENCES customer(id);
ALTER TABLE support_ticket ADD COLUMN IF NOT EXISTS resolution_note VARCHAR(2000);
ALTER TABLE support_ticket ADD COLUMN IF NOT EXISTS resolved_at TIMESTAMP;
ALTER TABLE support_ticket ADD COLUMN IF NOT EXISTS closed_at TIMESTAMP;
ALTER TABLE support_ticket ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE support_ticket ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE product_review ADD COLUMN IF NOT EXISTS id BIGSERIAL PRIMARY KEY;
ALTER TABLE product_review ADD COLUMN IF NOT EXISTS product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE;
ALTER TABLE product_review ADD COLUMN IF NOT EXISTS customer_id BIGINT NOT NULL REFERENCES customer(id);
ALTER TABLE product_review ADD COLUMN IF NOT EXISTS rating INT NOT NULL CHECK (rating BETWEEN 1 AND 5);
ALTER TABLE product_review ADD COLUMN IF NOT EXISTS comment VARCHAR(2000);
ALTER TABLE product_review ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED'));
ALTER TABLE product_review ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE product_review ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE provider ADD COLUMN IF NOT EXISTS id BIGSERIAL PRIMARY KEY;
ALTER TABLE provider ADD COLUMN IF NOT EXISTS name VARCHAR(255) NOT NULL;
ALTER TABLE provider ADD COLUMN IF NOT EXISTS contact_name VARCHAR(255) NOT NULL;
ALTER TABLE provider ADD COLUMN IF NOT EXISTS contact_email VARCHAR(255) NOT NULL;
ALTER TABLE provider ADD COLUMN IF NOT EXISTS description VARCHAR(2000);
ALTER TABLE provider ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'REGISTERED' CHECK (status IN ('REGISTERED', 'VERIFIED', 'APPROVED', 'ACTIVE', 'REJECTED'));
ALTER TABLE provider ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE provider ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE partner_contract ADD COLUMN IF NOT EXISTS id BIGSERIAL PRIMARY KEY;
ALTER TABLE partner_contract ADD COLUMN IF NOT EXISTS provider_id BIGINT NOT NULL UNIQUE REFERENCES provider(id) ON DELETE CASCADE;
ALTER TABLE partner_contract ADD COLUMN IF NOT EXISTS terms VARCHAR(4000) NOT NULL;
ALTER TABLE partner_contract ADD COLUMN IF NOT EXISTS start_date DATE NOT NULL;
ALTER TABLE partner_contract ADD COLUMN IF NOT EXISTS end_date DATE NOT NULL;
ALTER TABLE partner_contract ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'EXPIRED'));
ALTER TABLE partner_contract ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE partner_contract ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE billing_details ADD COLUMN IF NOT EXISTS id BIGSERIAL PRIMARY KEY;
ALTER TABLE billing_details ADD COLUMN IF NOT EXISTS owner_customer_id BIGINT REFERENCES customer(id);
ALTER TABLE billing_details ADD COLUMN IF NOT EXISTS owner_organization_id BIGINT REFERENCES organization(id);
ALTER TABLE billing_details ADD COLUMN IF NOT EXISTS billing_name VARCHAR(200) NOT NULL;
ALTER TABLE billing_details ADD COLUMN IF NOT EXISTS billing_email VARCHAR(255) NOT NULL;
ALTER TABLE billing_details ADD COLUMN IF NOT EXISTS address_line1 VARCHAR(200) NOT NULL;
ALTER TABLE billing_details ADD COLUMN IF NOT EXISTS address_line2 VARCHAR(200);
ALTER TABLE billing_details ADD COLUMN IF NOT EXISTS city VARCHAR(100) NOT NULL;
ALTER TABLE billing_details ADD COLUMN IF NOT EXISTS state VARCHAR(100) NOT NULL;
ALTER TABLE billing_details ADD COLUMN IF NOT EXISTS postal_code VARCHAR(20) NOT NULL;
ALTER TABLE billing_details ADD COLUMN IF NOT EXISTS country VARCHAR(100) NOT NULL;
ALTER TABLE billing_details ADD COLUMN IF NOT EXISTS tax_id VARCHAR(50);
ALTER TABLE billing_details ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE invoice ADD COLUMN IF NOT EXISTS id BIGSERIAL PRIMARY KEY;
ALTER TABLE invoice ADD COLUMN IF NOT EXISTS invoice_number VARCHAR(40) UNIQUE;
ALTER TABLE invoice ADD COLUMN IF NOT EXISTS owner_customer_id BIGINT REFERENCES customer(id);
ALTER TABLE invoice ADD COLUMN IF NOT EXISTS owner_organization_id BIGINT REFERENCES organization(id);
ALTER TABLE invoice ADD COLUMN IF NOT EXISTS subscription_id BIGINT NOT NULL REFERENCES product_subscription(id);
ALTER TABLE invoice ADD COLUMN IF NOT EXISTS status VARCHAR(25) NOT NULL DEFAULT 'OPEN' CHECK (status IN ('OPEN', 'PAID', 'PARTIALLY_REFUNDED', 'REFUNDED', 'VOID'));
ALTER TABLE invoice ADD COLUMN IF NOT EXISTS currency VARCHAR(10) NOT NULL;
ALTER TABLE invoice ADD COLUMN IF NOT EXISTS subtotal BIGINT NOT NULL;
ALTER TABLE invoice ADD COLUMN IF NOT EXISTS tax_amount BIGINT NOT NULL DEFAULT 0;
ALTER TABLE invoice ADD COLUMN IF NOT EXISTS total BIGINT NOT NULL;
ALTER TABLE invoice ADD COLUMN IF NOT EXISTS period_start TIMESTAMP;
ALTER TABLE invoice ADD COLUMN IF NOT EXISTS period_end TIMESTAMP;
ALTER TABLE invoice ADD COLUMN IF NOT EXISTS issued_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE invoice ADD COLUMN IF NOT EXISTS due_at TIMESTAMP;
ALTER TABLE invoice ADD COLUMN IF NOT EXISTS bill_to_snapshot VARCHAR(1000);
ALTER TABLE invoice ADD COLUMN IF NOT EXISTS payment_route VARCHAR(10) CHECK (payment_route IN ('ONLINE', 'OFFLINE'));
ALTER TABLE invoice ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE invoice_line ADD COLUMN IF NOT EXISTS id BIGSERIAL PRIMARY KEY;
ALTER TABLE invoice_line ADD COLUMN IF NOT EXISTS invoice_id BIGINT NOT NULL REFERENCES invoice(id) ON DELETE CASCADE;
ALTER TABLE invoice_line ADD COLUMN IF NOT EXISTS description VARCHAR(255) NOT NULL;
ALTER TABLE invoice_line ADD COLUMN IF NOT EXISTS period_start TIMESTAMP;
ALTER TABLE invoice_line ADD COLUMN IF NOT EXISTS period_end TIMESTAMP;
ALTER TABLE invoice_line ADD COLUMN IF NOT EXISTS quantity INT NOT NULL DEFAULT 1;
ALTER TABLE invoice_line ADD COLUMN IF NOT EXISTS unit_amount BIGINT NOT NULL;
ALTER TABLE invoice_line ADD COLUMN IF NOT EXISTS amount BIGINT NOT NULL;
ALTER TABLE invoice_line ADD COLUMN IF NOT EXISTS subscription_id BIGINT REFERENCES product_subscription(id);
ALTER TABLE payment ADD COLUMN IF NOT EXISTS id BIGSERIAL PRIMARY KEY;
ALTER TABLE payment ADD COLUMN IF NOT EXISTS invoice_id BIGINT NOT NULL REFERENCES invoice(id);
ALTER TABLE payment ADD COLUMN IF NOT EXISTS provider VARCHAR(20) NOT NULL DEFAULT 'RAZORPAY';
ALTER TABLE payment ADD COLUMN IF NOT EXISTS provider_order_id VARCHAR(100);
ALTER TABLE payment ADD COLUMN IF NOT EXISTS provider_payment_id VARCHAR(100);
ALTER TABLE payment ADD COLUMN IF NOT EXISTS status VARCHAR(25) NOT NULL DEFAULT 'CREATED' CHECK (status IN ('CREATED', 'CAPTURED', 'FAILED', 'PARTIALLY_REFUNDED', 'REFUNDED'));
ALTER TABLE payment ADD COLUMN IF NOT EXISTS currency VARCHAR(10) NOT NULL;
ALTER TABLE payment ADD COLUMN IF NOT EXISTS amount BIGINT NOT NULL;
ALTER TABLE payment ADD COLUMN IF NOT EXISTS refunded_amount BIGINT NOT NULL DEFAULT 0;
ALTER TABLE payment ADD COLUMN IF NOT EXISTS method_type VARCHAR(20);
ALTER TABLE payment ADD COLUMN IF NOT EXISTS method_network VARCHAR(40);
ALTER TABLE payment ADD COLUMN IF NOT EXISTS method_last4 VARCHAR(4);
ALTER TABLE payment ADD COLUMN IF NOT EXISTS failure_reason VARCHAR(500);
ALTER TABLE payment ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE payment ADD COLUMN IF NOT EXISTS captured_at TIMESTAMP;
ALTER TABLE payment ADD COLUMN IF NOT EXISTS offline_method VARCHAR(20) CHECK (offline_method IN ('BANK_TRANSFER', 'NEFT_RTGS', 'CHEQUE'));
ALTER TABLE payment ADD COLUMN IF NOT EXISTS offline_reference VARCHAR(100);
ALTER TABLE payment ADD COLUMN IF NOT EXISTS received_on DATE;
ALTER TABLE payment ADD COLUMN IF NOT EXISTS recorded_by_customer_id BIGINT REFERENCES customer(id);
ALTER TABLE payment ADD COLUMN IF NOT EXISTS note VARCHAR(500);
ALTER TABLE payment_refund ADD COLUMN IF NOT EXISTS id BIGSERIAL PRIMARY KEY;
ALTER TABLE payment_refund ADD COLUMN IF NOT EXISTS payment_id BIGINT NOT NULL REFERENCES payment(id);
ALTER TABLE payment_refund ADD COLUMN IF NOT EXISTS provider_refund_id VARCHAR(100);
ALTER TABLE payment_refund ADD COLUMN IF NOT EXISTS amount BIGINT NOT NULL;
ALTER TABLE payment_refund ADD COLUMN IF NOT EXISTS reason VARCHAR(500) NOT NULL;
ALTER TABLE payment_refund ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'PROCESSED';
ALTER TABLE payment_refund ADD COLUMN IF NOT EXISTS requested_by_customer_id BIGINT NOT NULL REFERENCES customer(id);
ALTER TABLE payment_refund ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE payment_method ADD COLUMN IF NOT EXISTS id BIGSERIAL PRIMARY KEY;
ALTER TABLE payment_method ADD COLUMN IF NOT EXISTS owner_customer_id BIGINT REFERENCES customer(id);
ALTER TABLE payment_method ADD COLUMN IF NOT EXISTS owner_organization_id BIGINT REFERENCES organization(id);
ALTER TABLE payment_method ADD COLUMN IF NOT EXISTS provider_token_ref VARCHAR(100) NOT NULL;
ALTER TABLE payment_method ADD COLUMN IF NOT EXISTS type VARCHAR(10) NOT NULL CHECK (type IN ('CARD', 'UPI'));
ALTER TABLE payment_method ADD COLUMN IF NOT EXISTS network VARCHAR(40);
ALTER TABLE payment_method ADD COLUMN IF NOT EXISTS last4 VARCHAR(4);
ALTER TABLE payment_method ADD COLUMN IF NOT EXISTS expiry_month INT;
ALTER TABLE payment_method ADD COLUMN IF NOT EXISTS expiry_year INT;
ALTER TABLE payment_method ADD COLUMN IF NOT EXISTS card_type VARCHAR(20);
ALTER TABLE payment_method ADD COLUMN IF NOT EXISTS issuer VARCHAR(100);
ALTER TABLE payment_method ADD COLUMN IF NOT EXISTS upi_masked VARCHAR(100);
ALTER TABLE payment_method ADD COLUMN IF NOT EXISTS is_default BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE payment_method ADD COLUMN IF NOT EXISTS consent_at TIMESTAMP;
ALTER TABLE payment_method ADD COLUMN IF NOT EXISTS status VARCHAR(10) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'REMOVED'));
ALTER TABLE payment_method ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE billing_settings ADD COLUMN IF NOT EXISTS id BIGSERIAL PRIMARY KEY;
ALTER TABLE billing_settings ADD COLUMN IF NOT EXISTS offline_account_name VARCHAR(200);
ALTER TABLE billing_settings ADD COLUMN IF NOT EXISTS offline_bank_name VARCHAR(200);
ALTER TABLE billing_settings ADD COLUMN IF NOT EXISTS offline_account_number VARCHAR(34);
ALTER TABLE billing_settings ADD COLUMN IF NOT EXISTS offline_ifsc VARCHAR(11);
ALTER TABLE billing_settings ADD COLUMN IF NOT EXISTS offline_swift_bic VARCHAR(11);
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
ALTER TABLE billing_settings ADD COLUMN IF NOT EXISTS invoice_prefix VARCHAR(10) NOT NULL DEFAULT 'INV';
ALTER TABLE billing_settings ADD COLUMN IF NOT EXISTS payment_terms_days INT NOT NULL DEFAULT 0 CHECK (payment_terms_days BETWEEN 0 AND 365);
ALTER TABLE billing_settings ADD COLUMN IF NOT EXISTS invoice_footer_note VARCHAR(500);
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
ALTER TABLE billing_settings ADD COLUMN IF NOT EXISTS method_card_enabled BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE billing_settings ADD COLUMN IF NOT EXISTS method_upi_enabled BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE billing_settings ADD COLUMN IF NOT EXISTS method_netbanking_enabled BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE billing_settings ADD COLUMN IF NOT EXISTS method_wallet_enabled BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE billing_settings ADD COLUMN IF NOT EXISTS method_pay_by_invoice_enabled BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE billing_settings ADD COLUMN IF NOT EXISTS checkout_display_name VARCHAR(100);
ALTER TABLE billing_settings ADD COLUMN IF NOT EXISTS checkout_description VARCHAR(255);
ALTER TABLE billing_settings ADD COLUMN IF NOT EXISTS checkout_theme_color VARCHAR(7);
ALTER TABLE billing_settings ADD COLUMN IF NOT EXISTS reminder_lead_days INT NOT NULL DEFAULT 7 CHECK (reminder_lead_days BETWEEN 1 AND 30);
ALTER TABLE billing_settings ADD COLUMN IF NOT EXISTS reminder_send_time VARCHAR(5) NOT NULL DEFAULT '09:00';
ALTER TABLE billing_settings ADD COLUMN IF NOT EXISTS reminder_time_zone VARCHAR(64) NOT NULL DEFAULT 'Asia/Kolkata';
ALTER TABLE billing_settings ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE billing_settings ADD COLUMN IF NOT EXISTS updated_by_customer_id BIGINT REFERENCES customer(id);
ALTER TABLE payment_webhook_event ADD COLUMN IF NOT EXISTS id BIGSERIAL PRIMARY KEY;
ALTER TABLE payment_webhook_event ADD COLUMN IF NOT EXISTS provider_event_id VARCHAR(100) NOT NULL UNIQUE;
ALTER TABLE payment_webhook_event ADD COLUMN IF NOT EXISTS event_type VARCHAR(50) NOT NULL;
ALTER TABLE payment_webhook_event ADD COLUMN IF NOT EXISTS payment_id BIGINT REFERENCES payment(id);
ALTER TABLE payment_webhook_event ADD COLUMN IF NOT EXISTS received_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE payment_webhook_event ADD COLUMN IF NOT EXISTS processed_at TIMESTAMP;
ALTER TABLE payment_webhook_event ADD COLUMN IF NOT EXISTS payload_summary VARCHAR(500);
ALTER TABLE cart ADD COLUMN IF NOT EXISTS id BIGSERIAL PRIMARY KEY;
ALTER TABLE cart ADD COLUMN IF NOT EXISTS customer_id BIGINT NOT NULL UNIQUE REFERENCES customer(id);
ALTER TABLE cart ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE cart ADD COLUMN IF NOT EXISTS last_checkout_kind VARCHAR(10) CHECK (last_checkout_kind IN ('INVOICE', 'ORDER'));
ALTER TABLE cart ADD COLUMN IF NOT EXISTS last_checkout_ref VARCHAR(500);
ALTER TABLE cart ADD COLUMN IF NOT EXISTS last_checkout_at TIMESTAMP;
ALTER TABLE cart_item ADD COLUMN IF NOT EXISTS id BIGSERIAL PRIMARY KEY;
ALTER TABLE cart_item ADD COLUMN IF NOT EXISTS cart_id BIGINT NOT NULL REFERENCES cart(id) ON DELETE CASCADE;
ALTER TABLE cart_item ADD COLUMN IF NOT EXISTS product_id BIGINT NOT NULL REFERENCES products(id);
ALTER TABLE cart_item ADD COLUMN IF NOT EXISTS plan_id BIGINT NOT NULL;
ALTER TABLE cart_item ADD COLUMN IF NOT EXISTS unit_price_at_add BIGINT NOT NULL;
ALTER TABLE cart_item ADD COLUMN IF NOT EXISTS currency VARCHAR(10) NOT NULL;
ALTER TABLE cart_item ADD COLUMN IF NOT EXISTS added_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE outbox_event ADD COLUMN IF NOT EXISTS id BIGSERIAL PRIMARY KEY;
ALTER TABLE outbox_event ADD COLUMN IF NOT EXISTS event_id VARCHAR(36) NOT NULL UNIQUE;
ALTER TABLE outbox_event ADD COLUMN IF NOT EXISTS event_type VARCHAR(100) NOT NULL;
ALTER TABLE outbox_event ADD COLUMN IF NOT EXISTS aggregate_type VARCHAR(100) NOT NULL;
ALTER TABLE outbox_event ADD COLUMN IF NOT EXISTS aggregate_id VARCHAR(100) NOT NULL;
ALTER TABLE outbox_event ADD COLUMN IF NOT EXISTS occurred_at TIMESTAMP NOT NULL;
ALTER TABLE outbox_event ADD COLUMN IF NOT EXISTS payload TEXT NOT NULL;
ALTER TABLE outbox_event ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'DELIVERED', 'FAILED'));
ALTER TABLE outbox_event ADD COLUMN IF NOT EXISTS attempts INT NOT NULL DEFAULT 0;
ALTER TABLE outbox_event ADD COLUMN IF NOT EXISTS next_attempt_at TIMESTAMP;
ALTER TABLE outbox_event ADD COLUMN IF NOT EXISTS last_error VARCHAR(1000);
ALTER TABLE outbox_event ADD COLUMN IF NOT EXISTS delivered_at TIMESTAMP;
ALTER TABLE outbox_event ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE event_handler_receipt ADD COLUMN IF NOT EXISTS id BIGSERIAL PRIMARY KEY;
ALTER TABLE event_handler_receipt ADD COLUMN IF NOT EXISTS handler_name VARCHAR(100) NOT NULL;
ALTER TABLE event_handler_receipt ADD COLUMN IF NOT EXISTS event_id VARCHAR(36) NOT NULL;
ALTER TABLE event_handler_receipt ADD COLUMN IF NOT EXISTS processed_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE api_key ADD COLUMN IF NOT EXISTS id BIGSERIAL PRIMARY KEY;
ALTER TABLE api_key ADD COLUMN IF NOT EXISTS owner_customer_id BIGINT NOT NULL REFERENCES customer(id);
ALTER TABLE api_key ADD COLUMN IF NOT EXISTS owner_keycloak_sub VARCHAR(255) NOT NULL;
ALTER TABLE api_key ADD COLUMN IF NOT EXISTS owner_authorities VARCHAR(1000);
ALTER TABLE api_key ADD COLUMN IF NOT EXISTS name VARCHAR(100) NOT NULL;
ALTER TABLE api_key ADD COLUMN IF NOT EXISTS key_prefix VARCHAR(20) NOT NULL UNIQUE;
ALTER TABLE api_key ADD COLUMN IF NOT EXISTS key_hash VARCHAR(64) NOT NULL UNIQUE;
ALTER TABLE api_key ADD COLUMN IF NOT EXISTS expires_at TIMESTAMP;
ALTER TABLE api_key ADD COLUMN IF NOT EXISTS revoked_at TIMESTAMP;
ALTER TABLE api_key ADD COLUMN IF NOT EXISTS last_used_at TIMESTAMP;
ALTER TABLE api_key ADD COLUMN IF NOT EXISTS request_count BIGINT NOT NULL DEFAULT 0;
ALTER TABLE api_key ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE renewal_reminder_preference ADD COLUMN IF NOT EXISTS id BIGSERIAL PRIMARY KEY;
ALTER TABLE renewal_reminder_preference ADD COLUMN IF NOT EXISTS customer_id BIGINT NOT NULL UNIQUE REFERENCES customer(id);
ALTER TABLE renewal_reminder_preference ADD COLUMN IF NOT EXISTS enabled BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE renewal_reminder_preference ADD COLUMN IF NOT EXISTS days_before INT CHECK (days_before BETWEEN 1 AND 30);
ALTER TABLE renewal_reminder_preference ADD COLUMN IF NOT EXISTS send_time VARCHAR(5);
ALTER TABLE renewal_reminder_preference ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE renewal_reminder_log ADD COLUMN IF NOT EXISTS id BIGSERIAL PRIMARY KEY;
ALTER TABLE renewal_reminder_log ADD COLUMN IF NOT EXISTS subscription_id BIGINT NOT NULL REFERENCES product_subscription(id);
ALTER TABLE renewal_reminder_log ADD COLUMN IF NOT EXISTS recipient_customer_id BIGINT NOT NULL REFERENCES customer(id);
ALTER TABLE renewal_reminder_log ADD COLUMN IF NOT EXISTS local_date DATE NOT NULL;
ALTER TABLE renewal_reminder_log ADD COLUMN IF NOT EXISTS renewal_date TIMESTAMP NOT NULL;
ALTER TABLE renewal_reminder_log ADD COLUMN IF NOT EXISTS days_before INT NOT NULL;
ALTER TABLE renewal_reminder_log ADD COLUMN IF NOT EXISTS sent_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE search_document ADD COLUMN IF NOT EXISTS id BIGSERIAL PRIMARY KEY;
ALTER TABLE search_document ADD COLUMN IF NOT EXISTS source_type VARCHAR(20) NOT NULL CHECK (source_type IN ('PRODUCT', 'KNOWLEDGE', 'TICKET'));
ALTER TABLE search_document ADD COLUMN IF NOT EXISTS source_id BIGINT NOT NULL;
ALTER TABLE search_document ADD COLUMN IF NOT EXISTS visibility VARCHAR(10) NOT NULL CHECK (visibility IN ('PUBLIC', 'OWNER'));
ALTER TABLE search_document ADD COLUMN IF NOT EXISTS owner_customer_id BIGINT;
ALTER TABLE search_document ADD COLUMN IF NOT EXISTS reference VARCHAR(40) NOT NULL;
ALTER TABLE search_document ADD COLUMN IF NOT EXISTS title VARCHAR(300) NOT NULL;
ALTER TABLE search_document ADD COLUMN IF NOT EXISTS body TEXT;
ALTER TABLE search_document ADD COLUMN IF NOT EXISTS keywords VARCHAR(2000);
ALTER TABLE search_document ADD COLUMN IF NOT EXISTS title_folded VARCHAR(300) NOT NULL;
ALTER TABLE search_document ADD COLUMN IF NOT EXISTS body_folded TEXT;
ALTER TABLE search_document ADD COLUMN IF NOT EXISTS keywords_folded VARCHAR(2000);
ALTER TABLE search_document ADD COLUMN IF NOT EXISTS content_updated_at TIMESTAMP;
ALTER TABLE search_document ADD COLUMN IF NOT EXISTS indexed_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE search_document ADD COLUMN IF NOT EXISTS tsv tsvector GENERATED ALWAYS AS ( setweight(to_tsvector('simple', coalesce(title_folded, '')), 'A') || setweight(to_tsvector('english', coalesce(title_folded, '')), 'A') || setweight(to_tsvector('spanish', coalesce(title_folded, '')), 'A') || setweight(to_tsvector('simple', coalesce(keywords_folded, '')), 'B') || setweight(to_tsvector('english', coalesce(keywords_folded, '')), 'B') || setweight(to_tsvector('spanish', coalesce(keywords_folded, '')), 'B') || setweight(to_tsvector('simple', coalesce(body_folded, '')), 'D') || setweight(to_tsvector('english', coalesce(body_folded, '')), 'C') || setweight(to_tsvector('spanish', coalesce(body_folded, '')), 'C') ) STORED;
ALTER TABLE search_term ADD COLUMN IF NOT EXISTS term VARCHAR(100) PRIMARY KEY;
ALTER TABLE search_term ADD COLUMN IF NOT EXISTS doc_count INT NOT NULL;
ALTER TABLE search_synonym ADD COLUMN IF NOT EXISTS id BIGSERIAL PRIMARY KEY;
ALTER TABLE search_synonym ADD COLUMN IF NOT EXISTS terms VARCHAR(500) NOT NULL;
ALTER TABLE search_synonym ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE search_query_log ADD COLUMN IF NOT EXISTS id BIGSERIAL PRIMARY KEY;
ALTER TABLE search_query_log ADD COLUMN IF NOT EXISTS query VARCHAR(200) NOT NULL;
ALTER TABLE search_query_log ADD COLUMN IF NOT EXISTS result_count INT NOT NULL;
ALTER TABLE search_query_log ADD COLUMN IF NOT EXISTS mode VARCHAR(10) NOT NULL;
ALTER TABLE search_query_log ADD COLUMN IF NOT EXISTS semantic_used BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE search_query_log ADD COLUMN IF NOT EXISTS took_ms INT NOT NULL;
ALTER TABLE search_query_log ADD COLUMN IF NOT EXISTS searched_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE search_index_run ADD COLUMN IF NOT EXISTS id BIGSERIAL PRIMARY KEY;
ALTER TABLE search_index_run ADD COLUMN IF NOT EXISTS trigger_type VARCHAR(10) NOT NULL CHECK (trigger_type IN ('BACKFILL', 'ADMIN'));
ALTER TABLE search_index_run ADD COLUMN IF NOT EXISTS status VARCHAR(10) NOT NULL CHECK (status IN ('RUNNING', 'DONE', 'FAILED'));
ALTER TABLE search_index_run ADD COLUMN IF NOT EXISTS documents INT NOT NULL DEFAULT 0;
ALTER TABLE search_index_run ADD COLUMN IF NOT EXISTS chunks INT NOT NULL DEFAULT 0;
ALTER TABLE search_index_run ADD COLUMN IF NOT EXISTS embedded INT NOT NULL DEFAULT 0;
ALTER TABLE search_index_run ADD COLUMN IF NOT EXISTS error VARCHAR(1000);
ALTER TABLE search_index_run ADD COLUMN IF NOT EXISTS started_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE search_index_run ADD COLUMN IF NOT EXISTS finished_at TIMESTAMP;
ALTER TABLE knowledge_content_version ADD COLUMN IF NOT EXISTS id BIGSERIAL PRIMARY KEY;
ALTER TABLE knowledge_content_version ADD COLUMN IF NOT EXISTS content_id BIGINT NOT NULL REFERENCES knowledge_article(id) ON DELETE CASCADE;
ALTER TABLE knowledge_content_version ADD COLUMN IF NOT EXISTS version_label VARCHAR(10) NOT NULL;
ALTER TABLE knowledge_content_version ADD COLUMN IF NOT EXISTS title VARCHAR(200) NOT NULL;
ALTER TABLE knowledge_content_version ADD COLUMN IF NOT EXISTS short_description VARCHAR(500);
ALTER TABLE knowledge_content_version ADD COLUMN IF NOT EXISTS blocks TEXT;
ALTER TABLE knowledge_content_version ADD COLUMN IF NOT EXISTS type_fields TEXT;
ALTER TABLE knowledge_content_version ADD COLUMN IF NOT EXISTS body VARCHAR(20000) NOT NULL;
ALTER TABLE knowledge_content_version ADD COLUMN IF NOT EXISTS search_text TEXT;
ALTER TABLE knowledge_content_version ADD COLUMN IF NOT EXISTS audience VARCHAR(20) NOT NULL DEFAULT 'PUBLIC';
ALTER TABLE knowledge_content_version ADD COLUMN IF NOT EXISTS audience_org_ids VARCHAR(1000);
ALTER TABLE knowledge_content_version ADD COLUMN IF NOT EXISTS require_product_access BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE knowledge_content_version ADD COLUMN IF NOT EXISTS effective_at TIMESTAMP;
ALTER TABLE knowledge_content_version ADD COLUMN IF NOT EXISTS expires_at TIMESTAMP;
ALTER TABLE knowledge_content_version ADD COLUMN IF NOT EXISTS published_by_sub VARCHAR(100);
ALTER TABLE knowledge_content_version ADD COLUMN IF NOT EXISTS published_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE knowledge_product ADD COLUMN IF NOT EXISTS id BIGSERIAL PRIMARY KEY;
ALTER TABLE knowledge_product ADD COLUMN IF NOT EXISTS name VARCHAR(100) NOT NULL;
ALTER TABLE knowledge_product ADD COLUMN IF NOT EXISTS slug VARCHAR(120) NOT NULL UNIQUE;
ALTER TABLE knowledge_product ADD COLUMN IF NOT EXISTS description VARCHAR(1000);
ALTER TABLE knowledge_product ADD COLUMN IF NOT EXISTS catalog_product_id BIGINT REFERENCES products(id) ON DELETE SET NULL;
ALTER TABLE knowledge_product ADD COLUMN IF NOT EXISTS display_order INT NOT NULL DEFAULT 0;
ALTER TABLE knowledge_product ADD COLUMN IF NOT EXISTS active BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE knowledge_module ADD COLUMN IF NOT EXISTS id BIGSERIAL PRIMARY KEY;
ALTER TABLE knowledge_module ADD COLUMN IF NOT EXISTS product_id BIGINT NOT NULL REFERENCES knowledge_product(id);
ALTER TABLE knowledge_module ADD COLUMN IF NOT EXISTS name VARCHAR(100) NOT NULL;
ALTER TABLE knowledge_module ADD COLUMN IF NOT EXISTS slug VARCHAR(120) NOT NULL;
ALTER TABLE knowledge_module ADD COLUMN IF NOT EXISTS display_order INT NOT NULL DEFAULT 0;
ALTER TABLE knowledge_module ADD COLUMN IF NOT EXISTS active BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE knowledge_category ADD COLUMN IF NOT EXISTS id BIGSERIAL PRIMARY KEY;
ALTER TABLE knowledge_category ADD COLUMN IF NOT EXISTS name VARCHAR(100) NOT NULL;
ALTER TABLE knowledge_category ADD COLUMN IF NOT EXISTS slug VARCHAR(120) NOT NULL;
ALTER TABLE knowledge_category ADD COLUMN IF NOT EXISTS scope VARCHAR(30);
ALTER TABLE knowledge_category ADD COLUMN IF NOT EXISTS display_order INT NOT NULL DEFAULT 0;
ALTER TABLE knowledge_category ADD COLUMN IF NOT EXISTS active BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE knowledge_media ADD COLUMN IF NOT EXISTS id BIGSERIAL PRIMARY KEY;
ALTER TABLE knowledge_media ADD COLUMN IF NOT EXISTS kind VARCHAR(20) NOT NULL;
ALTER TABLE knowledge_media ADD COLUMN IF NOT EXISTS storage_provider VARCHAR(20) NOT NULL;
ALTER TABLE knowledge_media ADD COLUMN IF NOT EXISTS s3_bucket VARCHAR(100);
ALTER TABLE knowledge_media ADD COLUMN IF NOT EXISTS s3_object_key VARCHAR(500) NOT NULL UNIQUE;
ALTER TABLE knowledge_media ADD COLUMN IF NOT EXISTS pending_object_key VARCHAR(500);
ALTER TABLE knowledge_media ADD COLUMN IF NOT EXISTS file_name VARCHAR(255) NOT NULL;
ALTER TABLE knowledge_media ADD COLUMN IF NOT EXISTS file_size BIGINT NOT NULL;
ALTER TABLE knowledge_media ADD COLUMN IF NOT EXISTS mime_type VARCHAR(100) NOT NULL;
ALTER TABLE knowledge_media ADD COLUMN IF NOT EXISTS etag VARCHAR(100);
ALTER TABLE knowledge_media ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'READY', 'FAILED', 'CANCELLED', 'DELETED'));
ALTER TABLE knowledge_media ADD COLUMN IF NOT EXISTS media_version INT NOT NULL DEFAULT 1;
ALTER TABLE knowledge_media ADD COLUMN IF NOT EXISTS product_id BIGINT;
ALTER TABLE knowledge_media ADD COLUMN IF NOT EXISTS module_id BIGINT;
ALTER TABLE knowledge_media ADD COLUMN IF NOT EXISTS upload_id VARCHAR(300);
ALTER TABLE knowledge_media ADD COLUMN IF NOT EXISTS uploaded_by_sub VARCHAR(100);
ALTER TABLE knowledge_media ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE knowledge_media ADD COLUMN IF NOT EXISTS uploaded_at TIMESTAMP;
ALTER TABLE knowledge_video ADD COLUMN IF NOT EXISTS id BIGSERIAL PRIMARY KEY;
ALTER TABLE knowledge_video ADD COLUMN IF NOT EXISTS content_id BIGINT NOT NULL UNIQUE REFERENCES knowledge_article(id) ON DELETE CASCADE;
ALTER TABLE knowledge_video ADD COLUMN IF NOT EXISTS video_source_type VARCHAR(20) NOT NULL CHECK (video_source_type IN ('YOUTUBE', 'AWS_S3', 'EXTERNAL_URL'));
ALTER TABLE knowledge_video ADD COLUMN IF NOT EXISTS video_url VARCHAR(1000);
ALTER TABLE knowledge_video ADD COLUMN IF NOT EXISTS video_id VARCHAR(20);
ALTER TABLE knowledge_video ADD COLUMN IF NOT EXISTS media_id BIGINT REFERENCES knowledge_media(id);
ALTER TABLE knowledge_video ADD COLUMN IF NOT EXISTS thumbnail_media_id BIGINT REFERENCES knowledge_media(id);
ALTER TABLE knowledge_video ADD COLUMN IF NOT EXISTS thumbnail_url VARCHAR(1000);
ALTER TABLE knowledge_video ADD COLUMN IF NOT EXISTS duration_seconds INT;
ALTER TABLE knowledge_video ADD COLUMN IF NOT EXISTS channel VARCHAR(200);
ALTER TABLE knowledge_video ADD COLUMN IF NOT EXISTS transcript TEXT;
ALTER TABLE knowledge_video ADD COLUMN IF NOT EXISTS chapters TEXT;
ALTER TABLE knowledge_video ADD COLUMN IF NOT EXISTS subtitles TEXT;
ALTER TABLE knowledge_feedback ADD COLUMN IF NOT EXISTS id BIGSERIAL PRIMARY KEY;
ALTER TABLE knowledge_feedback ADD COLUMN IF NOT EXISTS content_id BIGINT NOT NULL REFERENCES knowledge_article(id) ON DELETE CASCADE;
ALTER TABLE knowledge_feedback ADD COLUMN IF NOT EXISTS version_label VARCHAR(10);
ALTER TABLE knowledge_feedback ADD COLUMN IF NOT EXISTS customer_id BIGINT;
ALTER TABLE knowledge_feedback ADD COLUMN IF NOT EXISTS voter_sub VARCHAR(100);
ALTER TABLE knowledge_feedback ADD COLUMN IF NOT EXISTS kind VARCHAR(20) NOT NULL CHECK (kind IN ('VOTE', 'OUTDATED', 'SUGGESTION'));
ALTER TABLE knowledge_feedback ADD COLUMN IF NOT EXISTS helpful BOOLEAN;
ALTER TABLE knowledge_feedback ADD COLUMN IF NOT EXISTS reason VARCHAR(40);
ALTER TABLE knowledge_feedback ADD COLUMN IF NOT EXISTS comment_text VARCHAR(1000);
ALTER TABLE knowledge_feedback ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE knowledge_bookmark ADD COLUMN IF NOT EXISTS id BIGSERIAL PRIMARY KEY;
ALTER TABLE knowledge_bookmark ADD COLUMN IF NOT EXISTS customer_id BIGINT NOT NULL REFERENCES customer(id);
ALTER TABLE knowledge_bookmark ADD COLUMN IF NOT EXISTS content_id BIGINT NOT NULL REFERENCES knowledge_article(id) ON DELETE CASCADE;
ALTER TABLE knowledge_bookmark ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE knowledge_progress ADD COLUMN IF NOT EXISTS id BIGSERIAL PRIMARY KEY;
ALTER TABLE knowledge_progress ADD COLUMN IF NOT EXISTS customer_id BIGINT NOT NULL REFERENCES customer(id);
ALTER TABLE knowledge_progress ADD COLUMN IF NOT EXISTS content_id BIGINT NOT NULL REFERENCES knowledge_article(id) ON DELETE CASCADE;
ALTER TABLE knowledge_progress ADD COLUMN IF NOT EXISTS percent INT NOT NULL DEFAULT 0;
ALTER TABLE knowledge_progress ADD COLUMN IF NOT EXISTS position_seconds INT;
ALTER TABLE knowledge_progress ADD COLUMN IF NOT EXISTS completed_at TIMESTAMP;
ALTER TABLE knowledge_progress ADD COLUMN IF NOT EXISTS last_viewed_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE knowledge_event ADD COLUMN IF NOT EXISTS id BIGSERIAL PRIMARY KEY;
ALTER TABLE knowledge_event ADD COLUMN IF NOT EXISTS event_type VARCHAR(40) NOT NULL;
ALTER TABLE knowledge_event ADD COLUMN IF NOT EXISTS content_id BIGINT NOT NULL;
ALTER TABLE knowledge_event ADD COLUMN IF NOT EXISTS version_label VARCHAR(10);
ALTER TABLE knowledge_event ADD COLUMN IF NOT EXISTS content_type VARCHAR(30);
ALTER TABLE knowledge_event ADD COLUMN IF NOT EXISTS product_id BIGINT;
ALTER TABLE knowledge_event ADD COLUMN IF NOT EXISTS module_id BIGINT;
ALTER TABLE knowledge_event ADD COLUMN IF NOT EXISTS source_type VARCHAR(20);
ALTER TABLE knowledge_event ADD COLUMN IF NOT EXISTS organization_id BIGINT;
ALTER TABLE knowledge_event ADD COLUMN IF NOT EXISTS percent INT;
ALTER TABLE knowledge_event ADD COLUMN IF NOT EXISTS seconds INT;
ALTER TABLE knowledge_event ADD COLUMN IF NOT EXISTS viewer_hash VARCHAR(64);
ALTER TABLE knowledge_event ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE knowledge_course ADD COLUMN IF NOT EXISTS id BIGSERIAL PRIMARY KEY;
ALTER TABLE knowledge_course ADD COLUMN IF NOT EXISTS content_id BIGINT NOT NULL UNIQUE REFERENCES knowledge_article(id) ON DELETE CASCADE;
ALTER TABLE knowledge_course ADD COLUMN IF NOT EXISTS certificate_name VARCHAR(200);
ALTER TABLE knowledge_course ADD COLUMN IF NOT EXISTS pass_percent INT;
ALTER TABLE knowledge_lesson ADD COLUMN IF NOT EXISTS id BIGSERIAL PRIMARY KEY;
ALTER TABLE knowledge_lesson ADD COLUMN IF NOT EXISTS course_id BIGINT NOT NULL REFERENCES knowledge_course(id) ON DELETE CASCADE;
ALTER TABLE knowledge_lesson ADD COLUMN IF NOT EXISTS lesson_order INT NOT NULL;
ALTER TABLE knowledge_lesson ADD COLUMN IF NOT EXISTS content_id BIGINT REFERENCES knowledge_article(id);
ALTER TABLE knowledge_lesson ADD COLUMN IF NOT EXISTS title VARCHAR(200) NOT NULL;

-- 3. Indexes and remaining alterations
CREATE INDEX IF NOT EXISTS idx_product_plans_product_id ON product_plans (product_id);
CREATE UNIQUE INDEX IF NOT EXISTS idx_password_reset_token_hash ON password_reset_token (token_hash);
CREATE UNIQUE INDEX IF NOT EXISTS idx_customer_email ON customer (lower(email));
CREATE UNIQUE INDEX IF NOT EXISTS idx_customer_keycloak_sub ON customer (keycloak_sub) WHERE keycloak_sub IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_mfa_recovery_code_customer_id ON mfa_recovery_code (customer_id);
CREATE UNIQUE INDEX IF NOT EXISTS idx_organization_code ON organization (lower(code));
CREATE INDEX IF NOT EXISTS idx_saml_idp_organization_id ON saml_identity_provider (organization_id);
CREATE UNIQUE INDEX IF NOT EXISTS idx_saml_idp_one_enabled_per_org ON saml_identity_provider (organization_id) WHERE enabled = true;
CREATE UNIQUE INDEX IF NOT EXISTS idx_saml_external_identity_key ON saml_external_identity (organization_id, idp_entity_id, name_id);
CREATE INDEX IF NOT EXISTS idx_org_member_org ON organization_member (organization_id);
CREATE INDEX IF NOT EXISTS idx_org_member_customer ON organization_member (customer_id);
CREATE INDEX IF NOT EXISTS idx_org_product_access_member ON organization_product_access (organization_member_id);
CREATE INDEX IF NOT EXISTS idx_subscription_customer ON product_subscription (owner_customer_id);
CREATE INDEX IF NOT EXISTS idx_subscription_organization ON product_subscription (owner_organization_id);
CREATE UNIQUE INDEX IF NOT EXISTS idx_subscription_customer_product ON product_subscription (owner_customer_id, product_id) WHERE owner_customer_id IS NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS idx_subscription_org_product ON product_subscription (owner_organization_id, product_id) WHERE owner_organization_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_orders_organization ON orders (organization_id, status);
CREATE INDEX IF NOT EXISTS idx_orders_requested_by ON orders (requested_by_customer_id);
CREATE UNIQUE INDEX IF NOT EXISTS idx_verification_token_hash ON email_verification_token (token_hash);
CREATE UNIQUE INDEX IF NOT EXISTS idx_role_name ON role (name);
CREATE UNIQUE INDEX IF NOT EXISTS idx_permission_name ON permission (name);
CREATE INDEX IF NOT EXISTS idx_pa_request_requester ON privileged_access_request (requester_keycloak_sub);
CREATE INDEX IF NOT EXISTS idx_pa_request_org_pending ON privileged_access_request (scope, organization_id, status);
CREATE INDEX IF NOT EXISTS idx_pa_audit_request ON privileged_access_audit_entry (request_id);
CREATE UNIQUE INDEX IF NOT EXISTS idx_favorite_product_customer_product ON favorite_product (customer_id, product_id);
CREATE UNIQUE INDEX IF NOT EXISTS idx_product_usage_customer_product ON product_usage (customer_id, product_id);
CREATE INDEX IF NOT EXISTS idx_search_history_customer ON search_history_entry (customer_id, searched_at DESC);
CREATE INDEX IF NOT EXISTS idx_notification_customer ON notification (customer_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_audit_log_organization ON audit_log (organization_id, "timestamp" DESC);
CREATE INDEX IF NOT EXISTS idx_audit_log_actor ON audit_log (actor_customer_id, "timestamp" DESC);
CREATE INDEX IF NOT EXISTS idx_audit_log_timestamp ON audit_log ("timestamp" DESC);
CREATE INDEX IF NOT EXISTS idx_service_incident_product ON service_incident (product_id, started_at DESC);
CREATE UNIQUE INDEX IF NOT EXISTS idx_oidc_idp_one_enabled_per_org ON oidc_identity_provider (organization_id) WHERE enabled = true;
ALTER TABLE saml_identity_provider
    ADD COLUMN IF NOT EXISTS email_claim VARCHAR(255),
    ADD COLUMN IF NOT EXISTS first_name_claim VARCHAR(255),
    ADD COLUMN IF NOT EXISTS last_name_claim VARCHAR(255),
    ADD COLUMN IF NOT EXISTS display_name_claim VARCHAR(255);
ALTER TABLE oidc_identity_provider
    ADD COLUMN IF NOT EXISTS email_claim VARCHAR(255),
    ADD COLUMN IF NOT EXISTS first_name_claim VARCHAR(255),
    ADD COLUMN IF NOT EXISTS last_name_claim VARCHAR(255),
    ADD COLUMN IF NOT EXISTS display_name_claim VARCHAR(255);
CREATE INDEX IF NOT EXISTS idx_knowledge_article_status ON knowledge_article (status);
CREATE INDEX IF NOT EXISTS idx_support_ticket_requested_by ON support_ticket (requested_by_customer_id);
CREATE INDEX IF NOT EXISTS idx_support_ticket_status ON support_ticket (status);
CREATE INDEX IF NOT EXISTS idx_product_review_product_status ON product_review (product_id, status);
CREATE INDEX IF NOT EXISTS idx_partner_contract_status_end_date ON partner_contract (status, end_date);
CREATE UNIQUE INDEX IF NOT EXISTS idx_billing_details_customer ON billing_details (owner_customer_id) WHERE owner_customer_id IS NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS idx_billing_details_organization ON billing_details (owner_organization_id) WHERE owner_organization_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_invoice_customer_status ON invoice (owner_customer_id, status);
CREATE INDEX IF NOT EXISTS idx_invoice_organization_status ON invoice (owner_organization_id, status);
CREATE INDEX IF NOT EXISTS idx_invoice_line_invoice ON invoice_line (invoice_id);
CREATE INDEX IF NOT EXISTS idx_payment_invoice ON payment (invoice_id);
CREATE UNIQUE INDEX IF NOT EXISTS idx_payment_provider_order ON payment (provider_order_id) WHERE provider_order_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_payment_refund_payment ON payment_refund (payment_id);
CREATE INDEX IF NOT EXISTS idx_payment_method_customer_status ON payment_method (owner_customer_id, status);
CREATE INDEX IF NOT EXISTS idx_payment_method_organization_status ON payment_method (owner_organization_id, status);
CREATE INDEX IF NOT EXISTS idx_cart_item_cart ON cart_item (cart_id);
CREATE INDEX IF NOT EXISTS idx_outbox_event_status_next ON outbox_event (status, next_attempt_at);
CREATE INDEX IF NOT EXISTS idx_outbox_event_aggregate ON outbox_event (aggregate_type, aggregate_id, occurred_at);
CREATE INDEX IF NOT EXISTS idx_outbox_event_type ON outbox_event (event_type);
CREATE INDEX IF NOT EXISTS idx_api_key_owner ON api_key (owner_customer_id);
CREATE INDEX IF NOT EXISTS idx_renewal_reminder_log_subscription ON renewal_reminder_log (subscription_id);
CREATE INDEX IF NOT EXISTS idx_search_document_tsv ON search_document USING GIN (tsv);
CREATE INDEX IF NOT EXISTS idx_search_document_title_trgm ON search_document USING GIN (title_folded gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_search_document_visibility ON search_document (visibility, owner_customer_id);
CREATE INDEX IF NOT EXISTS idx_search_document_reference ON search_document (reference);
CREATE INDEX IF NOT EXISTS idx_search_term_trgm ON search_term USING GIN (term gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_search_query_log_searched_at ON search_query_log (searched_at);
ALTER TABLE knowledge_article ADD COLUMN IF NOT EXISTS content_type VARCHAR(30) NOT NULL DEFAULT 'ARTICLE';
ALTER TABLE knowledge_article ADD COLUMN IF NOT EXISTS slug VARCHAR(220);
ALTER TABLE knowledge_article ADD COLUMN IF NOT EXISTS short_description VARCHAR(500);
ALTER TABLE knowledge_article ADD COLUMN IF NOT EXISTS blocks TEXT;
ALTER TABLE knowledge_article ADD COLUMN IF NOT EXISTS type_fields TEXT;
ALTER TABLE knowledge_article ADD COLUMN IF NOT EXISTS product_id BIGINT;
ALTER TABLE knowledge_article ADD COLUMN IF NOT EXISTS module_id BIGINT;
ALTER TABLE knowledge_article ADD COLUMN IF NOT EXISTS category_id BIGINT;
ALTER TABLE knowledge_article ADD COLUMN IF NOT EXISTS feature VARCHAR(200);
ALTER TABLE knowledge_article ADD COLUMN IF NOT EXISTS tags VARCHAR(1000);
ALTER TABLE knowledge_article ADD COLUMN IF NOT EXISTS keywords VARCHAR(1000);
ALTER TABLE knowledge_article ADD COLUMN IF NOT EXISTS audience VARCHAR(20) NOT NULL DEFAULT 'PUBLIC';
ALTER TABLE knowledge_article ADD COLUMN IF NOT EXISTS audience_org_ids VARCHAR(1000);
ALTER TABLE knowledge_article ADD COLUMN IF NOT EXISTS require_product_access BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE knowledge_article ADD COLUMN IF NOT EXISTS workflow_state VARCHAR(20) NOT NULL DEFAULT 'DRAFT';
ALTER TABLE knowledge_article ADD COLUMN IF NOT EXISTS live_version_id BIGINT;
ALTER TABLE knowledge_article ADD COLUMN IF NOT EXISTS current_version_label VARCHAR(10) NOT NULL DEFAULT '0.1';
ALTER TABLE knowledge_article ADD COLUMN IF NOT EXISTS product_version VARCHAR(50);
ALTER TABLE knowledge_article ADD COLUMN IF NOT EXISTS documentation_version VARCHAR(50);
ALTER TABLE knowledge_article ADD COLUMN IF NOT EXISTS effective_at TIMESTAMP;
ALTER TABLE knowledge_article ADD COLUMN IF NOT EXISTS review_at TIMESTAMP;
ALTER TABLE knowledge_article ADD COLUMN IF NOT EXISTS expires_at TIMESTAMP;
ALTER TABLE knowledge_article ADD COLUMN IF NOT EXISTS scheduled_at TIMESTAMP;
ALTER TABLE knowledge_article ADD COLUMN IF NOT EXISTS scheduled_bump VARCHAR(10);
ALTER TABLE knowledge_article ADD COLUMN IF NOT EXISTS published_at TIMESTAMP;
ALTER TABLE knowledge_article ADD COLUMN IF NOT EXISTS deprecated_at TIMESTAMP;
ALTER TABLE knowledge_article ADD COLUMN IF NOT EXISTS author_sub VARCHAR(100);
ALTER TABLE knowledge_article ADD COLUMN IF NOT EXISTS reviewer_sub VARCHAR(100);
ALTER TABLE knowledge_article ADD COLUMN IF NOT EXISTS approver_sub VARCHAR(100);
ALTER TABLE knowledge_article ADD COLUMN IF NOT EXISTS updated_by_sub VARCHAR(100);
ALTER TABLE knowledge_article ADD COLUMN IF NOT EXISTS review_comment VARCHAR(1000);
ALTER TABLE knowledge_article ADD COLUMN IF NOT EXISTS featured BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE knowledge_article ADD COLUMN IF NOT EXISTS difficulty VARCHAR(20);
ALTER TABLE knowledge_article ADD COLUMN IF NOT EXISTS direct_action_route VARCHAR(300);
ALTER TABLE knowledge_article ADD COLUMN IF NOT EXISTS direct_action_label VARCHAR(100);
ALTER TABLE knowledge_article ADD COLUMN IF NOT EXISTS related_content_ids VARCHAR(1000);
ALTER TABLE knowledge_article ADD COLUMN IF NOT EXISTS search_text TEXT;
CREATE INDEX IF NOT EXISTS idx_knowledge_article_type_state ON knowledge_article (content_type, workflow_state);
CREATE INDEX IF NOT EXISTS idx_knowledge_article_product_module ON knowledge_article (product_id, module_id);
CREATE INDEX IF NOT EXISTS idx_knowledge_article_slug ON knowledge_article (content_type, slug);
CREATE INDEX IF NOT EXISTS idx_knowledge_media_status ON knowledge_media (status, created_at);
CREATE INDEX IF NOT EXISTS idx_knowledge_feedback_content ON knowledge_feedback (content_id, created_at);
CREATE INDEX IF NOT EXISTS idx_knowledge_event_content ON knowledge_event (content_id, created_at);
CREATE INDEX IF NOT EXISTS idx_knowledge_event_type ON knowledge_event (event_type, created_at);
ALTER TABLE search_query_log ADD COLUMN IF NOT EXISTS scope VARCHAR(20);
CREATE INDEX IF NOT EXISTS idx_search_query_log_scope ON search_query_log (scope, searched_at);

COMMIT;
