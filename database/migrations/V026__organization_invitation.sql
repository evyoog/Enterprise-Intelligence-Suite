-- REQ-TEN-008 Invite user (C84, 2026-10-08). Additive only. Also seeds nothing: INVITE_USERS is seeded at startup by RbacSeeder.
-- Mirrors backend/src/main/resources/db/schema.sql (Flyway is disabled; apply by hand).

SET search_path TO eis_platform, public;

-- Data model: docs/07-database/data-model/invitations.md.
CREATE TABLE IF NOT EXISTS organization_invitation (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL REFERENCES organization(id) ON DELETE CASCADE,
    email VARCHAR(255) NOT NULL,
    normalized_email VARCHAR(255) NOT NULL,
    org_role VARCHAR(20) NOT NULL,
    org_node_id BIGINT REFERENCES org_node(id) ON DELETE SET NULL,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'ACCEPTED', 'DECLINED', 'EXPIRED', 'REVOKED')),
    invited_by_customer_id BIGINT NOT NULL REFERENCES customer(id),
    accepted_by_customer_id BIGINT REFERENCES customer(id),
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    accepted_at TIMESTAMP,
    declined_at TIMESTAMP,
    revoked_at TIMESTAMP,
    last_sent_at TIMESTAMP NOT NULL DEFAULT now(),
    send_count INTEGER NOT NULL DEFAULT 1
);
CREATE UNIQUE INDEX IF NOT EXISTS ux_invitation_pending ON organization_invitation (organization_id, normalized_email) WHERE status = 'PENDING';
CREATE INDEX IF NOT EXISTS idx_invitation_org ON organization_invitation (organization_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_invitation_due ON organization_invitation (status, expires_at);

CREATE TABLE IF NOT EXISTS member_access_override (
    id BIGSERIAL PRIMARY KEY,
    organization_member_id BIGINT NOT NULL REFERENCES organization_member(id) ON DELETE CASCADE,
    item_type VARCHAR(20) NOT NULL DEFAULT 'PERMISSION',
    permission_code VARCHAR(60) NOT NULL,
    granted BOOLEAN NOT NULL,
    set_by_customer_id BIGINT REFERENCES customer(id),
    set_at TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (organization_member_id, item_type, permission_code)
);
