-- REQ-TEN-006 Organization hierarchy (C82, 2026-10-07). Additive only.
-- Mirrors backend/src/main/resources/db/schema.sql (Flyway is disabled; apply by hand).

SET search_path TO eis_platform, public;

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
