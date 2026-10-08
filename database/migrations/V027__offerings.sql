-- REQ-CAT-005 Offering management (C85, 2026-10-08). Additive only.
-- Mirrors backend/src/main/resources/db/schema.sql (Flyway is disabled; apply by hand).

SET search_path TO eis_platform, public;

-- Data model: docs/07-database/data-model/offerings.md.
CREATE TABLE IF NOT EXISTS offering (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    description VARCHAR(1000),
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'ACTIVE', 'RETIRED')),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);
CREATE UNIQUE INDEX IF NOT EXISTS ux_offering_name ON offering (lower(name));

CREATE TABLE IF NOT EXISTS offering_product (
    offering_id BIGINT NOT NULL REFERENCES offering(id) ON DELETE CASCADE,
    sort_order INTEGER NOT NULL,
    product_id BIGINT NOT NULL REFERENCES product(id),
    PRIMARY KEY (offering_id, sort_order)
);
CREATE INDEX IF NOT EXISTS idx_offering_product_product ON offering_product (product_id);

-- No row = the product is open to both individuals and organizations (no behaviour change).
CREATE TABLE IF NOT EXISTS product_eligibility (
    product_id BIGINT PRIMARY KEY REFERENCES product(id) ON DELETE CASCADE,
    audience VARCHAR(20) NOT NULL DEFAULT 'BOTH' CHECK (audience IN ('BOTH', 'INDIVIDUAL', 'ORGANIZATION'))
);

CREATE TABLE IF NOT EXISTS product_compatibility (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES product(id) ON DELETE CASCADE,
    works_with_product_id BIGINT NOT NULL REFERENCES product(id) ON DELETE CASCADE,
    UNIQUE (product_id, works_with_product_id),
    CHECK (product_id <> works_with_product_id)
);
