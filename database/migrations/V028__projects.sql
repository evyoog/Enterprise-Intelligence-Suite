-- REQ-TEN-009 Projects (C88, 2026-10-12). Additive only. A project is an organization group of kind PROJECT plus products.
-- Mirrors backend/src/main/resources/db/schema.sql (Flyway is disabled; apply by hand).

SET search_path TO eis_platform, public;

-- Data model: docs/07-database/data-model/projects.md.
ALTER TABLE organization_group ADD COLUMN IF NOT EXISTS kind VARCHAR(10) NOT NULL DEFAULT 'GROUP' CHECK (kind IN ('GROUP', 'PROJECT'));
ALTER TABLE organization_group ADD COLUMN IF NOT EXISTS description VARCHAR(500);

CREATE TABLE IF NOT EXISTS organization_group_product (
    id BIGSERIAL PRIMARY KEY,
    group_id BIGINT NOT NULL REFERENCES organization_group(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES products(id),
    product_role VARCHAR(50) NOT NULL,
    added_at TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (group_id, product_id)
);

-- Empty = direct assignment; set = the access comes from that project.
ALTER TABLE organization_product_access ADD COLUMN IF NOT EXISTS source_group_id BIGINT REFERENCES organization_group(id);
