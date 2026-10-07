CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(2000),
    price NUMERIC(12, 2) NOT NULL,
    image_url VARCHAR(500),
    launch_url VARCHAR(500),
    category VARCHAR(255),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    sso_connected BOOLEAN NOT NULL DEFAULT false,
    -- 03.01.02 Show featured products (sprint 2027.1.2, V010).
    featured BOOLEAN NOT NULL DEFAULT false,
    -- C66: showcase colour (null = inherit the platform's), feature tags
    -- (comma-separated) and resource links.
    accent_color VARCHAR(7) CHECK (accent_color ~ '^#[0-9A-Fa-f]{6}$'),
    feature_tags VARCHAR(1000),
    documentation_url VARCHAR(500),
    support_url VARCHAR(500),
    -- 02.01 Product Lifecycle & Structure (sprint 2026.4.1, V006).
    version INT NOT NULL DEFAULT 1,
    parent_product_id BIGINT REFERENCES products(id),
    variant_label VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

-- 02.01.02.03 Define dependencies (sprint 2026.4.1, V006). Advisory only —
-- see Product#dependsOn's own javadoc.
CREATE TABLE product_dependencies (
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    depends_on_product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    PRIMARY KEY (product_id, depends_on_product_id)
);

CREATE TABLE product_plans (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    price NUMERIC(12, 2) NOT NULL,
    billing_period VARCHAR(20) NOT NULL,
    sort_order INT,
    -- 02.03 Plan Management (sprint 2026.4.1, V006).
    currency VARCHAR(10) NOT NULL DEFAULT 'USD',
    usage_limit INT,
    included_features VARCHAR(1000),
    usage_price NUMERIC(12, 4),
    tier_pricing VARCHAR(500),
    overage_charge NUMERIC(12, 4)
);

CREATE INDEX idx_product_plans_product_id ON product_plans (product_id);

-- A "high-level platform" (e.g. Thittam) that apps are grouped under.
CREATE TABLE platforms (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(2000),
    image_url VARCHAR(500),
    -- C66: showcase colour and catalog settings.
    primary_color VARCHAR(7) CHECK (primary_color ~ '^#[0-9A-Fa-f]{6}$'),
    status VARCHAR(10) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE')),
    show_in_catalog BOOLEAN NOT NULL DEFAULT true,
    display_order INT NOT NULL DEFAULT 0 CHECK (display_order BETWEEN 0 AND 9999),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

-- Many-to-many: an app can be assigned to more than one platform.
CREATE TABLE product_platforms (
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    platform_id BIGINT NOT NULL REFERENCES platforms(id) ON DELETE CASCADE,
    PRIMARY KEY (product_id, platform_id)
);

-- 15.01 Platform Administration (sprint 2026.4.2). Seeded from the fixed
-- Currency enum — see PlatformCurrency's own javadoc; enable/disable only.
CREATE TABLE platform_currency (
    code VARCHAR(10) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT true
);

-- 15.01.02 Configure regions; 05.02.01.03 Assign region (organization.region_id
-- below) — freely admin-defined, not a fixed geography list.
CREATE TABLE platform_region (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT true
);

-- 15.01.01 Configure feature flags — runtime, admin-toggleable, distinct
-- from a Spring config property. See PlatformFeatureFlag's own javadoc.
CREATE TABLE platform_feature_flag (
    flag_key VARCHAR(100) PRIMARY KEY,
    enabled BOOLEAN NOT NULL DEFAULT true,
    description VARCHAR(500)
);

CREATE TABLE sso_bridge_session (
    id VARCHAR(64) PRIMARY KEY,
    keycloak_sub VARCHAR(255) NOT NULL,
    refresh_token TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL
);

-- Phase 8: single-use, expiring password-reset tokens — only a HASH of the
-- raw token is ever stored (see PasswordResetService), never the token
-- itself and never a password. Keyed by keycloak_sub, not a customer id —
-- applies to any real Keycloak account.
CREATE TABLE password_reset_token (
    id BIGSERIAL PRIMARY KEY,
    token_hash VARCHAR(255) NOT NULL,
    keycloak_sub VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);
CREATE UNIQUE INDEX idx_password_reset_token_hash ON password_reset_token (token_hash);

-- ============================================================
-- REGISTRATION & ONBOARDING (individual + organization customers)
-- Additive only — nothing above this line is touched. See
-- docs on RegistrationService/OrganizationSelfService for the flows that
-- populate these tables.
-- ============================================================

-- A Vyoog person identity created at registration — NOT a Keycloak user.
-- keycloak_sub stays NULL until a Vyoog (platform) admin manually creates the
-- matching Keycloak user and calls the admin "link" endpoint (see
-- AdminRegistrationController) — the deliberate seam for automatic Keycloak
-- provisioning later, keyed on sub, never on email.
CREATE TABLE customer (
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
CREATE UNIQUE INDEX idx_customer_email ON customer (lower(email));
CREATE UNIQUE INDEX idx_customer_keycloak_sub ON customer (keycloak_sub) WHERE keycloak_sub IS NOT NULL;

-- Phase 2 (2026.3.3): Platform-owned MFA (TOTP), layered on top of Keycloak's
-- own username/password authentication — see PlatformMfaService's own
-- javadoc for why. One row per customer; encrypted_secret holds either a
-- PENDING (enabled = false) or ACTIVE (enabled = true) secret — never
-- plaintext, always AES-256-GCM-encrypted at rest. A pending secret that
-- never gets verified is inert (enabled stays false, so it can never gate a
-- real login) and is simply overwritten the next time enrollment starts.
CREATE TABLE customer_mfa (
    customer_id BIGINT PRIMARY KEY REFERENCES customer(id) ON DELETE CASCADE,
    enabled BOOLEAN NOT NULL DEFAULT false,
    encrypted_secret TEXT,
    secret_set_at TIMESTAMP,
    enrolled_at TIMESTAMP,
    last_verified_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

-- Single-use recovery codes for Platform MFA — only a SHA-256 hash is ever
-- stored (same convention as password_reset_token/email_verification_token),
-- never the raw code. Regenerating deletes and replaces the whole set.
CREATE TABLE mfa_recovery_code (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES customer(id) ON DELETE CASCADE,
    code_hash VARCHAR(64) NOT NULL,
    used_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX idx_mfa_recovery_code_customer_id ON mfa_recovery_code (customer_id);

-- A pending login that passed Keycloak's own password check but is waiting
-- on the Platform TOTP second factor before a real session is created (see
-- AuthController#login / #verifyPlatformMfa). id is a random opaque token,
-- the only thing the browser ever sees — the real Keycloak tokens obtained
-- during the password grant are held here, server-side only, until the
-- challenge is satisfied or expires. Same "opaque id, raw token stays
-- server-side" shape as sso_bridge_session, deliberately.
CREATE TABLE mfa_login_challenge (
    id VARCHAR(64) PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES customer(id) ON DELETE CASCADE,
    keycloak_sub VARCHAR(255) NOT NULL,
    access_token TEXT NOT NULL,
    refresh_token TEXT NOT NULL,
    attempts INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    expires_at TIMESTAMP NOT NULL,
    -- C29 (2026-09-26): VERIFY (enter a code) or ENROLL (set up an
    -- authenticator during sign-in); impersonated = held tokens came from a
    -- SAML/OIDC token exchange. See migration V002.
    kind VARCHAR(10) NOT NULL DEFAULT 'VERIFY' CHECK (kind IN ('VERIFY', 'ENROLL')),
    impersonated BOOLEAN NOT NULL DEFAULT false
);

-- A registering company. parent_organization_id is NEVER settable by public
-- registration (see OrganizationRegistrationRequest / RegistrationService) —
-- Vyoog-Admin-managed only. Exists from day one so the relationship can be
-- populated later without a schema change; no tenant-provisioning logic is
-- attached to "code" in this phase — it's a plain unique business identifier.
CREATE TABLE organization (
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
    -- Phase 7 (MFA): ORG_ADMIN-settable, enforced at fresh login only — see
    -- AuthController#login / MfaPolicyService.
    mfa_required BOOLEAN NOT NULL DEFAULT false,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING_EMAIL_VERIFICATION',
    -- REQ-TEN-001 (2026.4.2, 05.01.01): platform-admin lifecycle, separate
    -- from the registration status above. See migration V001.
    lifecycle_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
        CHECK (lifecycle_status IN ('ACTIVE', 'SUSPENDED', 'CLOSED')),
    -- 05.02 Tenant Lifecycle (sprint 2026.4.2, carried from 2026.4.1).
    region_id BIGINT REFERENCES platform_region(id),
    allow_seat_overage BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);
CREATE UNIQUE INDEX idx_organization_code ON organization (lower(code));

-- Phase 4 (2026.3.3): Identity Federation foundation — an organization's own
-- external SAML IdP configuration, ORG_ADMIN self-service (same permission,
-- MANAGE_ORGANIZATION, as the MFA policy toggle above — "how my org's own
-- members authenticate" is the same class of setting). Multiple rows per
-- organization are allowed (e.g. staging a new IdP before cutting over), but
-- at most one may be ENABLED at a time — enforced by the partial unique
-- index below, since Phase 5's actual login needs an unambiguous IdP to
-- send the user to. Only a PEM certificate is stored, never a private key —
-- this app is always the SAML Service Provider, never the IdP, so it only
-- ever needs to verify the IdP's signature, never produce one of its own.
CREATE TABLE saml_identity_provider (
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
CREATE INDEX idx_saml_idp_organization_id ON saml_identity_provider (organization_id);
CREATE UNIQUE INDEX idx_saml_idp_one_enabled_per_org ON saml_identity_provider (organization_id) WHERE enabled = true;

-- Phase 5 (2026.3.3): the STABLE external-identity mapping — deliberately
-- keyed by (organization_id, idp_entity_id, name_id), never by
-- saml_provider_id (a row that can be deleted/recreated) and never by email
-- alone (an IdP-side email can change; NameID is the one thing SAML itself
-- defines as the stable subject identifier). Once a real Vyoog customer_id
-- is resolved for a given external identity, every future login from that
-- same (idp, subject) pair resolves to the SAME customer, never a duplicate.
CREATE TABLE saml_external_identity (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL REFERENCES organization(id) ON DELETE CASCADE,
    idp_entity_id VARCHAR(500) NOT NULL,
    name_id VARCHAR(500) NOT NULL,
    customer_id BIGINT NOT NULL REFERENCES customer(id) ON DELETE CASCADE,
    email VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    last_login_at TIMESTAMP
);
CREATE UNIQUE INDEX idx_saml_external_identity_key ON saml_external_identity (organization_id, idp_entity_id, name_id);

-- Phase 5: every AuthnRequest this app ever sends, so an incoming SAML
-- Response's InResponseTo can be checked against a request we genuinely
-- issued (rejects unsolicited responses), is still within its short
-- validity window, and — the actual replay defense — has not already been
-- consumed by an earlier successful login (consumed_at is set exactly once,
-- inside the same transaction that finalizes a session, never cleared).
CREATE TABLE saml_login_request (
    id VARCHAR(64) PRIMARY KEY,
    organization_id BIGINT NOT NULL REFERENCES organization(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    expires_at TIMESTAMP NOT NULL,
    consumed_at TIMESTAMP
);

-- One row per person per org — this is what a "licensed seat" is counted
-- against, never per-product (see organization_product_access below).
CREATE TABLE organization_member (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL REFERENCES organization(id) ON DELETE CASCADE,
    customer_id BIGINT NOT NULL REFERENCES customer(id),
    org_role VARCHAR(20) NOT NULL DEFAULT 'MEMBER',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    joined_at TIMESTAMP NOT NULL DEFAULT now(),
    deactivated_at TIMESTAMP,
    -- 05.03.02.03 Review access (sprint 2026.4.1, V006).
    last_reviewed_at TIMESTAMP,
    last_reviewed_by_customer_id BIGINT REFERENCES customer(id),
    UNIQUE (organization_id, customer_id)
);

-- 05.04.01 Groups (sprint 2026.4.1, V006).
CREATE TABLE organization_group (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL REFERENCES organization(id) ON DELETE CASCADE,
    name VARCHAR(150) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE organization_group_member (
    id BIGSERIAL PRIMARY KEY,
    group_id BIGINT NOT NULL REFERENCES organization_group(id) ON DELETE CASCADE,
    organization_member_id BIGINT NOT NULL REFERENCES organization_member(id) ON DELETE CASCADE,
    added_at TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (group_id, organization_member_id)
);
CREATE INDEX idx_org_member_org ON organization_member (organization_id);
CREATE INDEX idx_org_member_customer ON organization_member (customer_id);

-- A member's access to one specific product under one specific product-scoped
-- role (e.g. PMS_ADMIN). A row existing here is what "assigned" means — being
-- an org member never implies this, and the org holding a product_subscription
-- never implies this either (see the Product Suite's two-tier view).
CREATE TABLE organization_product_access (
    id BIGSERIAL PRIMARY KEY,
    organization_member_id BIGINT NOT NULL REFERENCES organization_member(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES products(id),
    product_role VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    assigned_at TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (organization_member_id, product_id)
);
CREATE INDEX idx_org_product_access_member ON organization_product_access (organization_member_id);

-- A subscription belongs to EXACTLY ONE of an individual customer or an
-- organization (enforced below) — references the EXISTING products table,
-- nothing about the catalog is duplicated here.
CREATE TABLE product_subscription (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id),
    owner_type VARCHAR(20) NOT NULL,
    owner_customer_id BIGINT REFERENCES customer(id),
    owner_organization_id BIGINT REFERENCES organization(id),
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING_SUBSCRIPTION',
    -- 07.01.02 Subscription Changes (sprint 2026.4.3).
    plan_id BIGINT REFERENCES product_plans(id),
    started_at TIMESTAMP,
    expires_at TIMESTAMP,
    -- REQ-SUB-003 (C63): seats of an organization subscription; 1 for individuals.
    quantity INT NOT NULL DEFAULT 1 CHECK (quantity BETWEEN 1 AND 100000),
    -- REQ-SUB-004 (C64): renewed automatically on expires_at.
    auto_renew BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT chk_subscription_owner CHECK (
        (owner_type = 'INDIVIDUAL' AND owner_customer_id IS NOT NULL AND owner_organization_id IS NULL)
        OR
        (owner_type = 'ORGANIZATION' AND owner_organization_id IS NOT NULL AND owner_customer_id IS NULL)
    )
);
CREATE INDEX idx_subscription_customer ON product_subscription (owner_customer_id);
CREATE INDEX idx_subscription_organization ON product_subscription (owner_organization_id);
CREATE UNIQUE INDEX idx_subscription_customer_product ON product_subscription (owner_customer_id, product_id) WHERE owner_customer_id IS NOT NULL;
CREATE UNIQUE INDEX idx_subscription_org_product ON product_subscription (owner_organization_id, product_id) WHERE owner_organization_id IS NOT NULL;

-- 09.01 Order Management (sprint 2027.1.1). Organization purchasing only —
-- see OrderService's own javadoc. No separate PROVISIONED status: APPROVED
-- already means provisioned (synchronous, no workflow engine — see V009).
CREATE TABLE orders (
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
CREATE INDEX idx_orders_organization ON orders (organization_id, status);
CREATE INDEX idx_orders_requested_by ON orders (requested_by_customer_id);

-- Single-use, expiring email verification tokens — only a HASH of the raw
-- token is ever stored; used_at is set once and never cleared, so a reused
-- link fails instead of silently no-op-ing.
CREATE TABLE email_verification_token (
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
CREATE UNIQUE INDEX idx_verification_token_hash ON email_verification_token (token_hash);

-- Phase 3 (RBAC): a Role is a named, scoped bundle of Permissions — real,
-- DB-editable data, seeded on every startup by RbacSeeder (see that class's
-- own javadoc). "Who holds a role" is NOT decided here: for PLATFORM scope
-- it's still Keycloak's client-role claim (role.name matches the Keycloak
-- role name, e.g. 'ADMIN'); for ORGANIZATION scope it's organization_member.org_role
-- (role.name matches that enum's values, e.g. 'ORG_ADMIN'). This table only
-- answers "what is a role allowed to do."
CREATE TABLE role (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(60) NOT NULL,
    scope VARCHAR(20) NOT NULL,
    description VARCHAR(255)
);
CREATE UNIQUE INDEX idx_role_name ON role (name);

CREATE TABLE permission (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(60) NOT NULL,
    description VARCHAR(255)
);
CREATE UNIQUE INDEX idx_permission_name ON permission (name);

CREATE TABLE role_permission (
    role_id BIGINT NOT NULL REFERENCES role(id),
    permission_id BIGINT NOT NULL REFERENCES permission(id),
    PRIMARY KEY (role_id, permission_id)
);

-- Phase 6 (PAM): "Normal user requests temporary admin access -> approval ->
-- temporary role granted -> access expires -> audit record retained," as a
-- request/decision lifecycle. Keyed by requester_keycloak_sub, not a
-- customer id, because a PLATFORM-scope requester may have no linked
-- customer row at all (see this table's own entity javadoc). No stored
-- EXPIRED status — a request past its expires_at while still APPROVED is
-- computed as expired at read/check time, never written back.
CREATE TABLE privileged_access_request (
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
CREATE INDEX idx_pa_request_requester ON privileged_access_request (requester_keycloak_sub);
CREATE INDEX idx_pa_request_org_pending ON privileged_access_request (scope, organization_id, status);

-- Immutable audit trail — one row per lifecycle transition, never updated or
-- deleted, so a request's history survives its own row being updated.
CREATE TABLE privileged_access_audit_entry (
    id BIGSERIAL PRIMARY KEY,
    request_id BIGINT NOT NULL REFERENCES privileged_access_request(id),
    event_type VARCHAR(20) NOT NULL,
    actor_keycloak_sub VARCHAR(255) NOT NULL,
    occurred_at TIMESTAMP NOT NULL DEFAULT now(),
    note VARCHAR(500)
);
CREATE INDEX idx_pa_audit_request ON privileged_access_audit_entry (request_id);

-- Phase 16: dashboard personalization. favorite_product/product_usage are
-- deliberately keyed by the plain catalog product_id, not any org-scoped
-- entitlement row — a personal bookmark/usage-count applies the same way
-- for an individual customer or an org member.
CREATE TABLE favorite_product (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES customer(id),
    product_id BIGINT NOT NULL REFERENCES products(id),
    created_at TIMESTAMP NOT NULL DEFAULT now()
);
CREATE UNIQUE INDEX idx_favorite_product_customer_product ON favorite_product (customer_id, product_id);

-- The real, minimal basis for "recently used" (order by last_launched_at)
-- and "frequently used" (order by launch_count) — updated every time the
-- frontend's own Launch button is clicked. No richer usage metric exists
-- anywhere in this app, and none is fabricated here.
CREATE TABLE product_usage (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES customer(id),
    product_id BIGINT NOT NULL REFERENCES products(id),
    last_launched_at TIMESTAMP NOT NULL DEFAULT now(),
    launch_count BIGINT NOT NULL DEFAULT 0
);
CREATE UNIQUE INDEX idx_product_usage_customer_product ON product_usage (customer_id, product_id);

-- One row per customer — widget visibility/ordering, stored as plain JSON
-- arrays of frontend-defined widget ids (see DashboardPreference's own
-- javadoc on why a fully normalized widget schema would be over-built here).
CREATE TABLE dashboard_preference (
    customer_id BIGINT PRIMARY KEY REFERENCES customer(id),
    widget_order_json TEXT,
    hidden_widgets_json TEXT,
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

-- Phase 6 (2026.3.3): a customer's own portal-wide personalization —
-- language/region/timezone/theme/reduced-motion — distinct from
-- dashboard_preference above (that one is widget layout only). Was
-- previously localStorage-only on the frontend (lost on a new device/browser,
-- never survived a fresh login) — this is what makes "customize preferences"
-- an actual account-level feature rather than a per-browser client hack. Null
-- columns mean "not set" (the frontend falls back to browser/OS defaults),
-- not "set to nothing" — see CustomerPreferenceDto's own javadoc.
CREATE TABLE customer_preference (
    customer_id BIGINT PRIMARY KEY REFERENCES customer(id),
    language VARCHAR(10),
    region VARCHAR(20),
    time_zone VARCHAR(64),
    theme_mode VARCHAR(10),
    reduced_motion BOOLEAN NOT NULL DEFAULT false,
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

-- Phase 17: what a customer actually typed into the storefront search box —
-- no foreign key to products (a query may match zero, and isn't "about" any
-- one product the way a favorite/launch is).
CREATE TABLE search_history_entry (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES customer(id),
    query VARCHAR(200) NOT NULL,
    searched_at TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX idx_search_history_customer ON search_history_entry (customer_id, searched_at DESC);

-- Phase 18: the notification center's own durable history — NOT recomputed
-- on read the way DashboardService's alerts are (see that table's own
-- comment); every row here is a real, already-occurred event.
CREATE TABLE notification (
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
CREATE INDEX idx_notification_customer ON notification (customer_id, created_at DESC);

-- One row per customer — which categories they've muted EMAIL for (in-app
-- notifications are always recorded regardless; see NotificationPreference's
-- own javadoc).
CREATE TABLE notification_preference (
    customer_id BIGINT PRIMARY KEY REFERENCES customer(id),
    email_disabled_categories_json TEXT,
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

-- Phase 25: the platform-wide audit trail. Deliberately NO foreign keys —
-- an audit record must stay readable even after the customer/organization it
-- refers to is later renamed or deleted, which is the whole point of an
-- audit trail (see AuditLog's own javadoc).
CREATE TABLE audit_log (
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
CREATE INDEX idx_audit_log_organization ON audit_log (organization_id, "timestamp" DESC);
CREATE INDEX idx_audit_log_actor ON audit_log (actor_customer_id, "timestamp" DESC);
CREATE INDEX idx_audit_log_timestamp ON audit_log ("timestamp" DESC);

-- REQ-PRT-001 interim service status page (sprint 2026.3.3, decisions C20/C26).
-- Posted by platform admins (MANAGE_SERVICE_STATUS). A product with no row is
-- OPERATIONAL. Replaced by Health Monitoring / Incident Management later (C20).
-- See migration V003.
CREATE TABLE product_service_status (
    product_id BIGINT PRIMARY KEY REFERENCES products(id) ON DELETE CASCADE,
    status VARCHAR(20) NOT NULL DEFAULT 'OPERATIONAL'
        CHECK (status IN ('OPERATIONAL', 'DEGRADED', 'PARTIAL_OUTAGE', 'MAJOR_OUTAGE', 'MAINTENANCE')),
    note VARCHAR(500),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_by_keycloak_sub VARCHAR(255)
);

CREATE TABLE service_incident (
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
CREATE INDEX idx_service_incident_product ON service_incident (product_id, started_at DESC);

-- REQ-IAM-006 OIDC federation (sprint 2026.3.3, decisions C22/C27), mirroring
-- the SAML tables. The client secret is AES-GCM encrypted (TotpSecretCipher).
-- At most one SAML or OIDC provider is enabled per organization (enforced in
-- SamlProviderService / OidcProviderService). See migration V004.
CREATE TABLE oidc_identity_provider (
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
CREATE UNIQUE INDEX idx_oidc_idp_one_enabled_per_org ON oidc_identity_provider (organization_id) WHERE enabled = true;

CREATE TABLE oidc_login_request (
    state VARCHAR(64) PRIMARY KEY,
    organization_id BIGINT NOT NULL REFERENCES organization(id) ON DELETE CASCADE,
    provider_id BIGINT NOT NULL REFERENCES oidc_identity_provider(id) ON DELETE CASCADE,
    nonce VARCHAR(64) NOT NULL,
    code_verifier VARCHAR(128) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    expires_at TIMESTAMP NOT NULL,
    consumed_at TIMESTAMP
);

CREATE TABLE oidc_external_identity (
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

-- REQ-IAM-007 claim mapping (sprint 2026.3.3, decisions C23/C28): optional
-- attribute / claim names per provider, tried before the defaults.
-- See migration V005.
ALTER TABLE saml_identity_provider
    ADD COLUMN email_claim VARCHAR(255),
    ADD COLUMN first_name_claim VARCHAR(255),
    ADD COLUMN last_name_claim VARCHAR(255),
    ADD COLUMN display_name_claim VARCHAR(255);
ALTER TABLE oidc_identity_provider
    ADD COLUMN email_claim VARCHAR(255),
    ADD COLUMN first_name_claim VARCHAR(255),
    ADD COLUMN last_name_claim VARCHAR(255),
    ADD COLUMN display_name_claim VARCHAR(255);

-- 11.01 Knowledge Base (sprint 2027.1.1): 11.01.02 AI Knowledge deliberately
-- not built — see KnowledgeArticle's own javadoc. See migration V009.
CREATE TABLE knowledge_article (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    body VARCHAR(20000) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'PUBLISHED')),
    version INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX idx_knowledge_article_status ON knowledge_article (status);

-- 12.01 Ticket Management (sprint 2027.1.2). Not AI-driven — see
-- SupportTicket's own javadoc. See migration V010.
CREATE TABLE support_ticket (
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
CREATE INDEX idx_support_ticket_requested_by ON support_ticket (requested_by_customer_id);
CREATE INDEX idx_support_ticket_status ON support_ticket (status);

-- 03.04 Reviews & Ratings (sprint 2027.1.3). PENDING until an admin
-- moderates it (MANAGE_REVIEWS) — never shown publicly or averaged before
-- then. At most one review per (product, customer) — see ProductReview's
-- own javadoc. See migration V011.
CREATE TABLE product_review (
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
CREATE INDEX idx_product_review_product_status ON product_review (product_id, status);

-- 14.01 Provider Onboarding (sprint 2027.2.1, C42). A provider applies
-- before it has any Vyoog identity — no FK to customer/organization, same
-- reasoning as the pre-login registration flow. See migration V012.
CREATE TABLE provider (
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

-- 14.01.02 Contracts. One contract per provider — Create/Manage terms are
-- the same upsert (see PartnerContract's own javadoc); Track expiration is
-- the scheduled ContractExpiryJob flipping status, not computed on read.
CREATE TABLE partner_contract (
    id BIGSERIAL PRIMARY KEY,
    provider_id BIGINT NOT NULL UNIQUE REFERENCES provider(id) ON DELETE CASCADE,
    terms VARCHAR(4000) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'EXPIRED')),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX idx_partner_contract_status_end_date ON partner_contract (status, end_date);

-- 08 Billing & Payments (sprint 2026.4.3, C46). Amounts are stored in the
-- currency's smallest unit (e.g. paise), not as a decimal price like
-- product_plans.price — billing arithmetic (refund remainders) needs exact
-- integers. Every owning table belongs to EXACTLY ONE of an individual
-- customer or an organization, same shape as product_subscription. See
-- migration V013.
CREATE TABLE billing_details (
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
CREATE UNIQUE INDEX idx_billing_details_customer ON billing_details (owner_customer_id) WHERE owner_customer_id IS NOT NULL;
CREATE UNIQUE INDEX idx_billing_details_organization ON billing_details (owner_organization_id) WHERE owner_organization_id IS NOT NULL;

CREATE TABLE invoice (
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
    -- C55 (REQ-BIL-001.19): what the customer chose at checkout; null until chosen.
    payment_route VARCHAR(10) CHECK (payment_route IN ('ONLINE', 'OFFLINE')),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    CHECK (num_nonnulls(owner_customer_id, owner_organization_id) = 1)
);
CREATE INDEX idx_invoice_customer_status ON invoice (owner_customer_id, status);
CREATE INDEX idx_invoice_organization_status ON invoice (owner_organization_id, status);

CREATE TABLE invoice_line (
    id BIGSERIAL PRIMARY KEY,
    invoice_id BIGINT NOT NULL REFERENCES invoice(id) ON DELETE CASCADE,
    description VARCHAR(255) NOT NULL,
    period_start TIMESTAMP,
    period_end TIMESTAMP,
    quantity INT NOT NULL DEFAULT 1,
    unit_amount BIGINT NOT NULL,
    amount BIGINT NOT NULL,
    -- C59 (REQ-MKT-003.8): the subscription this line bills, so one invoice
    -- can pay a cart of several products.
    subscription_id BIGINT REFERENCES product_subscription(id)
);
CREATE INDEX idx_invoice_line_invoice ON invoice_line (invoice_id);

CREATE TABLE payment (
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
    -- C55 (REQ-BIL-001.20): an offline payment recorded by a billing admin.
    offline_method VARCHAR(20) CHECK (offline_method IN ('BANK_TRANSFER', 'NEFT_RTGS', 'CHEQUE')),
    offline_reference VARCHAR(100),
    received_on DATE,
    recorded_by_customer_id BIGINT REFERENCES customer(id),
    note VARCHAR(500)
);
CREATE INDEX idx_payment_invoice ON payment (invoice_id);
CREATE UNIQUE INDEX idx_payment_provider_order ON payment (provider_order_id) WHERE provider_order_id IS NOT NULL;

CREATE TABLE payment_refund (
    id BIGSERIAL PRIMARY KEY,
    payment_id BIGINT NOT NULL REFERENCES payment(id),
    provider_refund_id VARCHAR(100),
    amount BIGINT NOT NULL,
    reason VARCHAR(500) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PROCESSED',
    requested_by_customer_id BIGINT NOT NULL REFERENCES customer(id),
    created_at TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX idx_payment_refund_payment ON payment_refund (payment_id);

CREATE TABLE payment_method (
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
CREATE INDEX idx_payment_method_customer_status ON payment_method (owner_customer_id, status);
CREATE INDEX idx_payment_method_organization_status ON payment_method (owner_organization_id, status);

-- C55 (REQ-BIL-001.21): offline bank details printed on offline invoices.
-- One platform-wide row. Not secrets (BR-SEC-001 does not apply).
CREATE TABLE billing_settings (
    id BIGSERIAL PRIMARY KEY,
    offline_account_name VARCHAR(200),
    offline_bank_name VARCHAR(200),
    offline_account_number VARCHAR(34),
    offline_ifsc VARCHAR(11),
    offline_swift_bic VARCHAR(11),
    -- C60: business profile (the invoice issuer)
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
    -- C60: invoicing
    invoice_prefix VARCHAR(10) NOT NULL DEFAULT 'INV',
    payment_terms_days INT NOT NULL DEFAULT 0 CHECK (payment_terms_days BETWEEN 0 AND 365),
    invoice_footer_note VARCHAR(500),
    -- C60: more offline payment details
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
    -- C60: checkout payment methods and Razorpay Checkout appearance (not secrets)
    method_card_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    method_upi_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    method_netbanking_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    method_wallet_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    method_pay_by_invoice_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    checkout_display_name VARCHAR(100),
    checkout_description VARCHAR(255),
    checkout_theme_color VARCHAR(7),
    -- REQ-SUB-004 (C64): renewal reminder defaults (proposed defaults — confirm).
    reminder_lead_days INT NOT NULL DEFAULT 7 CHECK (reminder_lead_days BETWEEN 1 AND 30),
    reminder_send_time VARCHAR(5) NOT NULL DEFAULT '09:00',
    reminder_time_zone VARCHAR(64) NOT NULL DEFAULT 'Asia/Kolkata',
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_by_customer_id BIGINT REFERENCES customer(id)
);

CREATE TABLE payment_webhook_event (
    id BIGSERIAL PRIMARY KEY,
    provider_event_id VARCHAR(100) NOT NULL UNIQUE,
    event_type VARCHAR(50) NOT NULL,
    payment_id BIGINT REFERENCES payment(id),
    received_at TIMESTAMP NOT NULL DEFAULT now(),
    processed_at TIMESTAMP,
    payload_summary VARCHAR(500)
);

-- C59 (REQ-MKT-003): the signed-in user's server-side cart.
CREATE TABLE cart (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL UNIQUE REFERENCES customer(id),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    last_checkout_kind VARCHAR(10) CHECK (last_checkout_kind IN ('INVOICE', 'ORDER')),
    last_checkout_ref VARCHAR(500),
    last_checkout_at TIMESTAMP
);

CREATE TABLE cart_item (
    id BIGSERIAL PRIMARY KEY,
    cart_id BIGINT NOT NULL REFERENCES cart(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES products(id),
    -- No FK: a deleted plan must show as NOT_AVAILABLE (BR-6 a), not block the delete.
    plan_id BIGINT NOT NULL,
    unit_price_at_add BIGINT NOT NULL,
    currency VARCHAR(10) NOT NULL,
    added_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uq_cart_item_product UNIQUE (cart_id, product_id)
);
CREATE INDEX idx_cart_item_cart ON cart_item (cart_id);

-- REQ-INT-002 (C62): platform events — transactional outbox and handler receipts.
CREATE TABLE outbox_event (
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
CREATE INDEX idx_outbox_event_status_next ON outbox_event (status, next_attempt_at);
CREATE INDEX idx_outbox_event_aggregate ON outbox_event (aggregate_type, aggregate_id, occurred_at);
CREATE INDEX idx_outbox_event_type ON outbox_event (event_type);

CREATE TABLE event_handler_receipt (
    id BIGSERIAL PRIMARY KEY,
    handler_name VARCHAR(100) NOT NULL,
    event_id VARCHAR(36) NOT NULL,
    processed_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uq_event_handler_receipt UNIQUE (handler_name, event_id)
);

-- REQ-INT-001 (C61): API keys. Only the prefix and SHA-256 hash are stored.
CREATE TABLE api_key (
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
CREATE INDEX idx_api_key_owner ON api_key (owner_customer_id);

-- REQ-SUB-004 (C64): renewal reminder settings per user and the sent-reminder log.
CREATE TABLE renewal_reminder_preference (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL UNIQUE REFERENCES customer(id),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    days_before INT CHECK (days_before BETWEEN 1 AND 30),
    send_time VARCHAR(5),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE renewal_reminder_log (
    id BIGSERIAL PRIMARY KEY,
    subscription_id BIGINT NOT NULL REFERENCES product_subscription(id),
    recipient_customer_id BIGINT NOT NULL REFERENCES customer(id),
    local_date DATE NOT NULL,
    renewal_date TIMESTAMP NOT NULL,
    days_before INT NOT NULL,
    sent_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uq_renewal_reminder_log UNIQUE (subscription_id, recipient_customer_id, local_date)
);
CREATE INDEX idx_renewal_reminder_log_subscription ON renewal_reminder_log (subscription_id);

-- REQ-PRT-002 / REQ-PRT-003 (C70, 2026-10-05): search index.
-- Part 1 — keyword search (needs pg_trgm, part of PostgreSQL contrib).
-- One row per searchable record. *_folded columns hold lower-case text with
-- accents removed (done by the backend), so "facturacion" finds "facturación".
-- visibility PUBLIC = anyone (catalog, published articles); OWNER = only
-- owner_customer_id (support tickets). Results are filtered on these two
-- columns before they are ranked.
CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE TABLE search_document (
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
CREATE INDEX idx_search_document_tsv ON search_document USING GIN (tsv);
CREATE INDEX idx_search_document_title_trgm ON search_document USING GIN (title_folded gin_trgm_ops);
CREATE INDEX idx_search_document_visibility ON search_document (visibility, owner_customer_id);
CREATE INDEX idx_search_document_reference ON search_document (reference);

-- Words of PUBLIC documents only, for "Did you mean". Private ticket words
-- are never added, so a suggestion can never reveal another user's ticket.
CREATE TABLE search_term (
    term VARCHAR(100) PRIMARY KEY,
    doc_count INT NOT NULL
);
CREATE INDEX idx_search_term_trgm ON search_term USING GIN (term gin_trgm_ops);

-- Admin-managed synonym groups: comma-separated equivalent terms.
CREATE TABLE search_synonym (
    id BIGSERIAL PRIMARY KEY,
    terms VARCHAR(500) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

-- Search insights: one row per search run from the results page. No user
-- identity is stored.
CREATE TABLE search_query_log (
    id BIGSERIAL PRIMARY KEY,
    query VARCHAR(200) NOT NULL,
    result_count INT NOT NULL,
    mode VARCHAR(10) NOT NULL,
    semantic_used BOOLEAN NOT NULL DEFAULT FALSE,
    took_ms INT NOT NULL,
    searched_at TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX idx_search_query_log_searched_at ON search_query_log (searched_at);

-- Admin "Rebuild index" runs.
CREATE TABLE search_index_run (
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

-- Part 2 — semantic search (needs the pgvector extension). If pgvector is
-- not available, skip this part: the backend detects the missing table and
-- runs keyword search only. Only PUBLIC documents get chunks/embeddings.
-- vector(384): the dimension of the embedding model (C70).
CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE search_chunk (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT NOT NULL REFERENCES search_document(id) ON DELETE CASCADE,
    chunk_index INT NOT NULL,
    content TEXT NOT NULL,
    content_hash VARCHAR(64) NOT NULL,
    embedding vector(384),
    embedded_at TIMESTAMP,
    CONSTRAINT uq_search_chunk UNIQUE (document_id, chunk_index)
);
CREATE INDEX idx_search_chunk_embedding ON search_chunk USING hnsw (embedding vector_cosine_ops);
CREATE INDEX idx_search_chunk_pending ON search_chunk (document_id) WHERE embedding IS NULL;

-- REQ-KNW-001 to REQ-KNW-008 (C71-C77, 2026-10-05): Knowledge Center and
-- Knowledge Management CMS. See migration V021 and
-- docs/07-database/data-model/knowledge.md.
-- REQ-KNW-001.7 / REQ-KNW-002: knowledge_article becomes the content table
-- (additive; existing rows keep ids, title, body, status and version).
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

-- Immutable published versions (BR-KCON-004); readers see the live one.
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

-- Taxonomy as data (C74).
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

-- Media in the private S3 bucket: metadata only, never files or URLs (BR-MED-001).
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
CREATE INDEX IF NOT EXISTS idx_knowledge_media_status ON knowledge_media (status, created_at);

-- Video source and text tracks of VIDEO content (REQ-KNW-004, BR-KVID-001).
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

-- Reader feedback, bookmarks, progress and analytics events (REQ-KNW-005/006).
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
CREATE INDEX IF NOT EXISTS idx_knowledge_feedback_content ON knowledge_feedback (content_id, created_at);
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
CREATE INDEX IF NOT EXISTS idx_knowledge_event_content ON knowledge_event (content_id, created_at);
CREATE INDEX IF NOT EXISTS idx_knowledge_event_type ON knowledge_event (event_type, created_at);

-- Knowledge Center searches are logged with scope KNOWLEDGE (REQ-KNW-006.2).
ALTER TABLE search_query_log ADD COLUMN IF NOT EXISTS scope VARCHAR(20);
CREATE INDEX IF NOT EXISTS idx_search_query_log_scope ON search_query_log (scope, searched_at);

-- Academy (11b, REQ-KNW-005.22): prepared only, not used until confirmed (C77).
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

-- REQ-CAT-004 / 02.04 Product content (C81, 2026-10-07): datasheets,
-- documentation links, images, videos and case studies per catalog product.
-- Files are referenced by private S3 object key only. Additive; mirrors
-- database/migrations/V024__product_content.sql. Data model:
-- docs/07-database/data-model/product-content.md.
CREATE TABLE IF NOT EXISTS product_content_item (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    kind VARCHAR(20) NOT NULL CHECK (kind IN ('DATASHEET', 'DOCUMENTATION', 'IMAGE', 'VIDEO', 'CASE_STUDY')),
    title VARCHAR(200) NOT NULL,
    description VARCHAR(1000),
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'PUBLISHED')),
    display_order INT NOT NULL DEFAULT 0,
    content_version INT NOT NULL DEFAULT 1,
    object_key VARCHAR(500) UNIQUE,
    file_name VARCHAR(255),
    file_size BIGINT,
    mime_type VARCHAR(100),
    alt_text VARCHAR(250),
    logo_object_key VARCHAR(500) UNIQUE,
    logo_file_name VARCHAR(255),
    logo_file_size BIGINT,
    logo_mime_type VARCHAR(100),
    video_provider VARCHAR(20) CHECK (video_provider IN ('YOUTUBE', 'VIMEO', 'EXTERNAL')),
    video_url VARCHAR(1000),
    video_ref VARCHAR(50),
    thumbnail_url VARCHAR(1000),
    customer_name VARCHAR(200),
    problem TEXT,
    result_text TEXT,
    knowledge_content_id BIGINT REFERENCES knowledge_article(id) ON DELETE SET NULL,
    published_at TIMESTAMP,
    created_by_sub VARCHAR(100),
    updated_by_sub VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_product_content_product ON product_content_item (product_id, status, display_order);

-- REQ-TEN-006 Organization hierarchy (C82). Additive; mirrors database/migrations/V025__org_hierarchy.sql.
-- Data model: docs/07-database/data-model/org-hierarchy.md.
CREATE TABLE IF NOT EXISTS org_node (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL REFERENCES organization(id) ON DELETE CASCADE,
    parent_id BIGINT REFERENCES org_node(id) ON DELETE RESTRICT,
    name VARCHAR(150) NOT NULL,
    node_type VARCHAR(50) NOT NULL,
    code VARCHAR(50),
    description VARCHAR(1000),
    sort_order INTEGER NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_org_node_parent ON org_node (organization_id, parent_id);
CREATE UNIQUE INDEX IF NOT EXISTS ux_org_node_root ON org_node (organization_id) WHERE parent_id IS NULL;
CREATE UNIQUE INDEX IF NOT EXISTS ux_org_node_sibling_name ON org_node (organization_id, parent_id, lower(name)) WHERE parent_id IS NOT NULL;

CREATE TABLE IF NOT EXISTS org_level (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL REFERENCES organization(id) ON DELETE CASCADE,
    node_type VARCHAR(50) NOT NULL,
    label VARCHAR(100) NOT NULL,
    level_rank INTEGER NOT NULL,
    UNIQUE (organization_id, node_type),
    UNIQUE (organization_id, level_rank) DEFERRABLE INITIALLY DEFERRED
);

CREATE TABLE IF NOT EXISTS org_node_history (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL REFERENCES organization(id) ON DELETE CASCADE,
    org_node_id BIGINT NOT NULL REFERENCES org_node(id) ON DELETE CASCADE,
    previous_parent_id BIGINT,
    new_parent_id BIGINT,
    changed_by_customer_id BIGINT,
    effective_at TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_org_node_history_node ON org_node_history (org_node_id, effective_at DESC);

ALTER TABLE organization_member ADD COLUMN IF NOT EXISTS org_node_id BIGINT REFERENCES org_node(id) ON DELETE SET NULL;
CREATE INDEX IF NOT EXISTS idx_org_member_node ON organization_member (org_node_id);
