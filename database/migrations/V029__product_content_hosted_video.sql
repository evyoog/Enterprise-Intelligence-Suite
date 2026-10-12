-- REQ-CAT-004.6 (C89, 2026-10-12): a product video can also be a file uploaded to the product storage.
-- Additive: the allowed values of video_provider gain HOSTED. Mirrors backend/src/main/resources/db/schema.sql
-- (Flyway is disabled; apply by hand). Data model: docs/07-database/data-model/product-content.md.

SET search_path TO eis_platform, public;

ALTER TABLE product_content_item DROP CONSTRAINT IF EXISTS product_content_item_video_provider_check;
ALTER TABLE product_content_item ADD CONSTRAINT product_content_item_video_provider_check
    CHECK (video_provider IN ('YOUTUBE', 'VIMEO', 'EXTERNAL', 'HOSTED'));
