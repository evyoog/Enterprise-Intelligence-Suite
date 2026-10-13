-- REQ-INT-001 (C92, API-1 B and API-4 B, 2026-10-13): API keys belong to an organization and are created by its
-- administrators; a key has a scope, READ (read-only) or READ_WRITE. Mirrors backend/src/main/resources/db/schema.sql
-- (Flyway is disabled; apply by hand). Data model: docs/07-database/data-model/api-management.md.
--
-- Existing keys keep working with the full rights they had (READ_WRITE). A key whose owner is an active organization
-- administrator moves to that organization; a key of anyone else stays a personal ("legacy") key until it is revoked or
-- expires, and no new personal key can be created.

SET search_path TO eis_platform, public;

ALTER TABLE api_key ADD COLUMN IF NOT EXISTS organization_id BIGINT REFERENCES organization(id);
ALTER TABLE api_key ADD COLUMN IF NOT EXISTS scope VARCHAR(20) NOT NULL DEFAULT 'READ_WRITE';
ALTER TABLE api_key ADD COLUMN IF NOT EXISTS created_by_customer_id BIGINT;
ALTER TABLE api_key DROP CONSTRAINT IF EXISTS api_key_scope_check;
ALTER TABLE api_key ADD CONSTRAINT api_key_scope_check CHECK (scope IN ('READ', 'READ_WRITE'));

UPDATE api_key SET created_by_customer_id = owner_customer_id WHERE created_by_customer_id IS NULL;

UPDATE api_key k
   SET organization_id = m.organization_id
  FROM organization_member m
 WHERE k.organization_id IS NULL
   AND m.customer_id = k.owner_customer_id
   AND m.status = 'ACTIVE'
   AND m.org_role = 'ORG_ADMIN';

CREATE INDEX IF NOT EXISTS idx_api_key_organization ON api_key (organization_id);
