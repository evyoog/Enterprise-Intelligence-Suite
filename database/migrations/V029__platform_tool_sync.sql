-- REQ-INT-003 platform ↔ tool synchronization, phase 7 (2026-10-09). Additive only.
-- Mirrors backend/src/main/resources/db/schema.sql (Flyway is disabled; apply by hand). Safe to run twice.
-- Data model: docs/07-database/data-model/platform-tool-sync.md.

SET search_path TO eis_platform, public;

-- 1. A per-aggregate version that only goes up. Incremented by the application inside the transaction of every change
--    (JPA callback, ToolSyncListener); the contract's "version" (platform ↔ tool contract v1, section 8).
ALTER TABLE organization                 ADD COLUMN IF NOT EXISTS sync_version BIGINT NOT NULL DEFAULT 1;
ALTER TABLE org_node                     ADD COLUMN IF NOT EXISTS sync_version BIGINT NOT NULL DEFAULT 1;
ALTER TABLE customer                     ADD COLUMN IF NOT EXISTS sync_version BIGINT NOT NULL DEFAULT 1;
ALTER TABLE organization_member          ADD COLUMN IF NOT EXISTS sync_version BIGINT NOT NULL DEFAULT 1;
ALTER TABLE product_subscription         ADD COLUMN IF NOT EXISTS sync_version BIGINT NOT NULL DEFAULT 1;
ALTER TABLE organization_product_access  ADD COLUMN IF NOT EXISTS sync_version BIGINT NOT NULL DEFAULT 1;

-- 2. The tools (products with a synchronized application). product_code is the code in every message ("thittam").
--    client_id is the tool's Keycloak service client: the azp the platform accepts on /api/mcp from that tool.
CREATE TABLE IF NOT EXISTS tool_connector (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL UNIQUE REFERENCES products(id),
    product_code VARCHAR(100) NOT NULL UNIQUE,
    base_mcp_url VARCHAR(500) NOT NULL,
    client_id VARCHAR(100) NOT NULL,
    contract_version VARCHAR(10) NOT NULL DEFAULT '1',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'PAUSED')),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

-- 3. Where an organization's data lives in a tool, and how far it has been synchronized.
CREATE TABLE IF NOT EXISTS tenant_app_schema (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL REFERENCES organization(id),
    product_id BIGINT NOT NULL REFERENCES products(id),
    tenant_ref VARCHAR(100) NOT NULL,
    datasource_ref VARCHAR(60) NOT NULL DEFAULT 'default',
    schema_name VARCHAR(63),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'READY', 'FAILED')),
    schema_version VARCHAR(50),
    last_synced_version BIGINT NOT NULL DEFAULT 0,
    last_synced_at TIMESTAMP,
    last_error VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (organization_id, product_id)
);

-- 4. One row per message to one tool for one organization: the per-destination status the monitor shows, retried on its own
--    schedule so a stopped tool never delays another (BR-SYN-005, BR-SYN-006). The message is built from the CURRENT state when it is sent.
CREATE TABLE IF NOT EXISTS tool_delivery (
    id BIGSERIAL PRIMARY KEY,
    event_id VARCHAR(36) NOT NULL,
    tool_connector_id BIGINT NOT NULL REFERENCES tool_connector(id),
    organization_id BIGINT NOT NULL,
    tenant_ref VARCHAR(100) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    aggregate_type VARCHAR(100) NOT NULL,
    aggregate_id VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'DELIVERED', 'FAILED')),
    attempts INT NOT NULL DEFAULT 0,
    next_attempt_at TIMESTAMP,
    last_error VARCHAR(1000),
    sent_version BIGINT,
    aggregate_version BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    delivered_at TIMESTAMP,
    UNIQUE (event_id, tool_connector_id, organization_id)
);
CREATE INDEX IF NOT EXISTS idx_tool_delivery_due ON tool_delivery (status, next_attempt_at);
CREATE INDEX IF NOT EXISTS idx_tool_delivery_aggregate ON tool_delivery (tool_connector_id, organization_id, aggregate_type, aggregate_id);

-- 5. A tool's call to the platform is answered with its FIRST result when it is repeated with the same key (kept at least 7 days).
CREATE TABLE IF NOT EXISTS mcp_idempotency (
    idempotency_key VARCHAR(100) NOT NULL,
    tool VARCHAR(100) NOT NULL,
    result_json TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY (idempotency_key, tool)
);
CREATE INDEX IF NOT EXISTS idx_mcp_idempotency_created ON mcp_idempotency (created_at);
