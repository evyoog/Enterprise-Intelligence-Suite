-- REQ-INT-002 (C62): platform events — transactional outbox and handler
-- receipts. REQ-INT-001 (C61): API keys. Mirrors
-- backend/src/main/resources/db/schema.sql (Flyway is disabled; apply by hand).

SET search_path TO eis_platform;

CREATE TABLE IF NOT EXISTS outbox_event (
    id BIGSERIAL PRIMARY KEY,
    event_id VARCHAR(36) NOT NULL UNIQUE,
    event_type VARCHAR(100) NOT NULL,
    aggregate_type VARCHAR(100) NOT NULL,
    aggregate_id VARCHAR(100) NOT NULL,
    occurred_at TIMESTAMP NOT NULL,
    payload TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'DELIVERED', 'FAILED')),
    attempts INT NOT NULL DEFAULT 0,
    next_attempt_at TIMESTAMP,
    last_error VARCHAR(1000),
    delivered_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_outbox_event_status_next ON outbox_event (status, next_attempt_at);
CREATE INDEX IF NOT EXISTS idx_outbox_event_aggregate ON outbox_event (aggregate_type, aggregate_id, occurred_at);
CREATE INDEX IF NOT EXISTS idx_outbox_event_type ON outbox_event (event_type);

CREATE TABLE IF NOT EXISTS event_handler_receipt (
    id BIGSERIAL PRIMARY KEY,
    handler_name VARCHAR(100) NOT NULL,
    event_id VARCHAR(36) NOT NULL,
    processed_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uq_event_handler_receipt UNIQUE (handler_name, event_id)
);

-- REQ-INT-001 (C61): API keys. Only the prefix and SHA-256 hash are stored.
CREATE TABLE IF NOT EXISTS api_key (
    id BIGSERIAL PRIMARY KEY,
    owner_customer_id BIGINT NOT NULL REFERENCES customer(id),
    owner_keycloak_sub VARCHAR(255) NOT NULL,
    owner_authorities VARCHAR(1000),
    name VARCHAR(100) NOT NULL,
    key_prefix VARCHAR(20) NOT NULL UNIQUE,
    key_hash VARCHAR(64) NOT NULL UNIQUE,
    expires_at TIMESTAMP,
    revoked_at TIMESTAMP,
    last_used_at TIMESTAMP,
    request_count BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_api_key_owner ON api_key (owner_customer_id);
