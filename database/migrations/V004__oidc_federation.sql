-- REQ-IAM-006 OIDC federation (sprint 2026.3.3, C22/C27).
-- Mirrors backend/src/main/resources/db/schema.sql.

SET search_path TO eis_platform;
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
CREATE UNIQUE INDEX IF NOT EXISTS idx_oidc_idp_one_enabled_per_org ON oidc_identity_provider (organization_id) WHERE enabled = true;

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
