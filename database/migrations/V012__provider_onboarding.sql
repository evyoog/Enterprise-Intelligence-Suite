-- Sprint 2027.2.1: 14.01 Provider Onboarding (Provider Lifecycle + Contracts).
-- Mirrors backend/src/main/resources/db/schema.sql.

SET search_path TO eis_platform;

CREATE TABLE IF NOT EXISTS provider (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    contact_name VARCHAR(255) NOT NULL,
    contact_email VARCHAR(255) NOT NULL,
    description VARCHAR(2000),
    status VARCHAR(20) NOT NULL DEFAULT 'REGISTERED'
        CHECK (status IN ('REGISTERED', 'VERIFIED', 'APPROVED', 'ACTIVE', 'REJECTED')),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS partner_contract (
    id BIGSERIAL PRIMARY KEY,
    provider_id BIGINT NOT NULL UNIQUE REFERENCES provider(id) ON DELETE CASCADE,
    terms VARCHAR(4000) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'EXPIRED')),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_partner_contract_status_end_date ON partner_contract (status, end_date);
