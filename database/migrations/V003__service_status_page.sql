-- REQ-PRT-001 interim service status page (sprint 2026.3.3, C20/C26).
-- Mirrors backend/src/main/resources/db/schema.sql. The MANAGE_SERVICE_STATUS
-- permission is added to the ADMIN role by RbacSeeder at startup.

SET search_path TO eis_platform;

CREATE TABLE IF NOT EXISTS product_service_status (
    product_id BIGINT PRIMARY KEY REFERENCES products(id) ON DELETE CASCADE,
    status VARCHAR(20) NOT NULL DEFAULT 'OPERATIONAL'
        CHECK (status IN ('OPERATIONAL', 'DEGRADED', 'PARTIAL_OUTAGE', 'MAJOR_OUTAGE', 'MAINTENANCE')),
    note VARCHAR(500),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_by_keycloak_sub VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS service_incident (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    title VARCHAR(200) NOT NULL,
    message VARCHAR(4000) NOT NULL,
    started_at TIMESTAMP NOT NULL,
    ended_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    created_by_keycloak_sub VARCHAR(255),
    CHECK (ended_at IS NULL OR ended_at >= started_at)
);
CREATE INDEX IF NOT EXISTS idx_service_incident_product ON service_incident (product_id, started_at DESC);
