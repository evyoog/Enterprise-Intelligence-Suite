-- REQ-TEN-001 Organization lifecycle (sprint 2026.4.2, functions 05.01.01.02-05).
-- Platform-admin lifecycle for an organization, kept separate from its
-- registration status. Every existing organization starts ACTIVE.
-- Mirrors backend/src/main/resources/db/schema.sql.

SET search_path TO eis_platform;

ALTER TABLE organization
    ADD COLUMN IF NOT EXISTS lifecycle_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE';

ALTER TABLE organization
    ADD CONSTRAINT organization_lifecycle_status_check
        CHECK (lifecycle_status IN ('ACTIVE', 'SUSPENDED', 'CLOSED'));
