-- C66 (2026-10-03): catalog showcase — platform colour and catalog settings,
-- app accent colour, feature tags and resource links. All additive and
-- optional; existing rows keep their behaviour (ACTIVE, shown in catalog,
-- default colour). Mirrors backend/src/main/resources/db/schema.sql
-- (Flyway is disabled; apply by hand).

SET search_path TO eis_platform;

ALTER TABLE platforms ADD COLUMN IF NOT EXISTS primary_color VARCHAR(7);
ALTER TABLE platforms DROP CONSTRAINT IF EXISTS chk_platforms_primary_color;
ALTER TABLE platforms ADD CONSTRAINT chk_platforms_primary_color CHECK (primary_color ~ '^#[0-9A-Fa-f]{6}$');
ALTER TABLE platforms ADD COLUMN IF NOT EXISTS status VARCHAR(10) NOT NULL DEFAULT 'ACTIVE';
ALTER TABLE platforms DROP CONSTRAINT IF EXISTS chk_platforms_status;
ALTER TABLE platforms ADD CONSTRAINT chk_platforms_status CHECK (status IN ('ACTIVE', 'INACTIVE'));
ALTER TABLE platforms ADD COLUMN IF NOT EXISTS show_in_catalog BOOLEAN NOT NULL DEFAULT true;
ALTER TABLE platforms ADD COLUMN IF NOT EXISTS display_order INT NOT NULL DEFAULT 0;
ALTER TABLE platforms DROP CONSTRAINT IF EXISTS chk_platforms_display_order;
ALTER TABLE platforms ADD CONSTRAINT chk_platforms_display_order CHECK (display_order BETWEEN 0 AND 9999);

ALTER TABLE products ADD COLUMN IF NOT EXISTS accent_color VARCHAR(7);
ALTER TABLE products DROP CONSTRAINT IF EXISTS chk_products_accent_color;
ALTER TABLE products ADD CONSTRAINT chk_products_accent_color CHECK (accent_color ~ '^#[0-9A-Fa-f]{6}$');
ALTER TABLE products ADD COLUMN IF NOT EXISTS feature_tags VARCHAR(1000);
ALTER TABLE products ADD COLUMN IF NOT EXISTS documentation_url VARCHAR(500);
ALTER TABLE products ADD COLUMN IF NOT EXISTS support_url VARCHAR(500);
