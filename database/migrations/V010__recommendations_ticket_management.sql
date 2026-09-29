-- Sprint 2027.1.2: 03.01.02 Recommendations (featured products flag; popular
-- products read from existing product_usage, no new table needed),
-- 12.01.01 Ticket Management. Mirrors backend/src/main/resources/db/schema.sql.

SET search_path TO eis_platform;

ALTER TABLE products
    ADD COLUMN IF NOT EXISTS featured BOOLEAN NOT NULL DEFAULT false;

CREATE TABLE IF NOT EXISTS support_ticket (
    id BIGSERIAL PRIMARY KEY,
    requested_by_customer_id BIGINT NOT NULL REFERENCES customer(id),
    subject VARCHAR(200) NOT NULL,
    description VARCHAR(4000) NOT NULL,
    category VARCHAR(100),
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM' CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'URGENT')),
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN'
        CHECK (status IN ('OPEN', 'IN_PROGRESS', 'ESCALATED', 'RESOLVED', 'CLOSED')),
    assigned_to_customer_id BIGINT REFERENCES customer(id),
    resolution_note VARCHAR(2000),
    resolved_at TIMESTAMP,
    closed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_support_ticket_requested_by ON support_ticket (requested_by_customer_id);
CREATE INDEX IF NOT EXISTS idx_support_ticket_status ON support_ticket (status);
