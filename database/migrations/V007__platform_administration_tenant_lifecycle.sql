-- Sprint 2026.4.2: 15.01 Platform Administration (currencies, regions,
-- feature flags), 05.02 Tenant Lifecycle (region assignment, tenant
-- policies), carried from 2026.4.1.
-- Mirrors backend/src/main/resources/db/schema.sql.

SET search_path TO eis_platform;

CREATE TABLE IF NOT EXISTS platform_currency (
    code VARCHAR(10) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT true
);

CREATE TABLE IF NOT EXISTS platform_region (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT true
);

CREATE TABLE IF NOT EXISTS platform_feature_flag (
    flag_key VARCHAR(100) PRIMARY KEY,
    enabled BOOLEAN NOT NULL DEFAULT true,
    description VARCHAR(500)
);

ALTER TABLE organization
    ADD COLUMN IF NOT EXISTS region_id BIGINT REFERENCES platform_region(id),
    ADD COLUMN IF NOT EXISTS allow_seat_overage BOOLEAN NOT NULL DEFAULT false;

-- Seed the fixed currency set (idempotent) — mirrors PlatformAdministrationSeeder,
-- which also runs this on every backend startup; this migration just makes sure
-- the rows exist even before the app's own seeder has run once.
INSERT INTO platform_currency (code, name, enabled) VALUES
    ('USD', 'US Dollar', true),
    ('EUR', 'Euro', true),
    ('GBP', 'British Pound', true),
    ('INR', 'Indian Rupee', true)
ON CONFLICT (code) DO NOTHING;

INSERT INTO platform_feature_flag (flag_key, enabled, description) VALUES
    ('groups_enabled', true, '05.04.01 Groups — organization admins can create groups and add/remove members.')
ON CONFLICT (flag_key) DO NOTHING;
