-- Sprint 2027.1.1: 09.01 Order Management (organization purchasing only,
-- 09.02/09.04 folded in synchronously — see OrderService's own javadoc),
-- 11.01 Knowledge Base (Knowledge Articles only, no AI indexing).
-- Mirrors backend/src/main/resources/db/schema.sql.

SET search_path TO eis_platform;

CREATE TABLE IF NOT EXISTS orders (
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
CREATE INDEX IF NOT EXISTS idx_orders_organization ON orders (organization_id, status);
CREATE INDEX IF NOT EXISTS idx_orders_requested_by ON orders (requested_by_customer_id);

CREATE TABLE IF NOT EXISTS knowledge_article (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    body VARCHAR(20000) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'PUBLISHED')),
    version INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_knowledge_article_status ON knowledge_article (status);
