-- Sprint 2027.1.1 revisit (C103, REQ-TEN-010): a member can ask for personal access to a product their organization
-- already subscribes to, and a pending order is reminded after 3 days. Additive and re-runnable. Apply by hand (Flyway
-- is disabled), after V042.

CREATE TABLE IF NOT EXISTS access_request (
    id BIGSERIAL PRIMARY KEY,
    organization_member_id BIGINT NOT NULL REFERENCES organization_member(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES products(id),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED')),
    message VARCHAR(500),
    decided_by_customer_id BIGINT,
    decided_at TIMESTAMP,
    decision_note VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_access_request_member ON access_request (organization_member_id);
-- One pending request per member and product.
CREATE UNIQUE INDEX IF NOT EXISTS uq_access_request_pending ON access_request (organization_member_id, product_id) WHERE status = 'PENDING';

ALTER TABLE orders ADD COLUMN IF NOT EXISTS reminded_at TIMESTAMP;
