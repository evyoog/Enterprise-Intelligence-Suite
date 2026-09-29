-- Sprint 2027.1.3: 03.04 Reviews & Ratings. 01.03 Global Search adds no new
-- table (a pure read aggregator over existing data). Mirrors
-- backend/src/main/resources/db/schema.sql.

SET search_path TO eis_platform;

CREATE TABLE IF NOT EXISTS product_review (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    customer_id BIGINT NOT NULL REFERENCES customer(id),
    rating INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment VARCHAR(2000),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (product_id, customer_id)
);
CREATE INDEX IF NOT EXISTS idx_product_review_product_status ON product_review (product_id, status);
