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
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE product_plans (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    price NUMERIC(12, 2) NOT NULL,
    billing_period VARCHAR(20) NOT NULL,
    sort_order INT
);

CREATE INDEX idx_product_plans_product_id ON product_plans (product_id);

-- A "high-level platform" (e.g. Thittam) that apps are grouped under.
CREATE TABLE platforms (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(2000),
    image_url VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

-- Many-to-many: an app can be assigned to more than one platform.
CREATE TABLE product_platforms (
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    platform_id BIGINT NOT NULL REFERENCES platforms(id) ON DELETE CASCADE,
    PRIMARY KEY (product_id, platform_id)
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
    UNIQUE (organization_id, customer_id)
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
    started_at TIMESTAMP,
    expires_at TIMESTAMP,
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
