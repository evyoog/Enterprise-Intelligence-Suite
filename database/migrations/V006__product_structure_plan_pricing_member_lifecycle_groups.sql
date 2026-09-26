-- Sprint 2026.4.1: 02.01 Product Lifecycle & Structure, 02.03 Plan Management,
-- 05.03 User Management (suspend/reactivate/remove, review access), 05.04 Groups.
-- Mirrors backend/src/main/resources/db/schema.sql.

SET search_path TO eis_platform;

-- 02.01 Product Lifecycle & Structure
ALTER TABLE products
    ADD COLUMN IF NOT EXISTS version INT NOT NULL DEFAULT 1,
    ADD COLUMN IF NOT EXISTS parent_product_id BIGINT REFERENCES products(id),
    ADD COLUMN IF NOT EXISTS variant_label VARCHAR(100);

CREATE TABLE IF NOT EXISTS product_dependencies (
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    depends_on_product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    PRIMARY KEY (product_id, depends_on_product_id)
);

-- 02.03 Plan Management
ALTER TABLE product_plans
    ADD COLUMN IF NOT EXISTS currency VARCHAR(10) NOT NULL DEFAULT 'USD',
    ADD COLUMN IF NOT EXISTS usage_limit INT,
    ADD COLUMN IF NOT EXISTS included_features VARCHAR(1000),
    ADD COLUMN IF NOT EXISTS usage_price NUMERIC(12, 4),
    ADD COLUMN IF NOT EXISTS tier_pricing VARCHAR(500),
    ADD COLUMN IF NOT EXISTS overage_charge NUMERIC(12, 4);

-- 05.03.01/05.03.02 User Lifecycle, Review access. The existing status
-- column already accepts the new SUSPENDED value (VARCHAR(20)) with no
-- length change needed.
ALTER TABLE organization_member
    ADD COLUMN IF NOT EXISTS last_reviewed_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS last_reviewed_by_customer_id BIGINT REFERENCES customer(id);

-- 05.04.01 Groups
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
