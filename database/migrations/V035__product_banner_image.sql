-- C95 Banner image for the catalog card (2026-10-13). Additive. Mirrors backend/src/main/resources/db/schema.sql (Flyway is disabled; apply by hand).
-- products.image_url stays the app icon; banner_image_url is the wide image behind the card.

SET search_path TO eis_platform, public;

ALTER TABLE products ADD COLUMN IF NOT EXISTS banner_image_url VARCHAR(500);
