-- REQ-CAT-004 Product content (C81, 2026-10-07). Additive only.
-- Mirrors backend/src/main/resources/db/schema.sql (Flyway is disabled; apply by hand).

SET search_path TO eis_platform, public;

-- Data model: docs/07-database/data-model/product-content.md.
CREATE TABLE IF NOT EXISTS product_content_item (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    kind VARCHAR(20) NOT NULL CHECK (kind IN ('DATASHEET', 'DOCUMENTATION', 'IMAGE', 'VIDEO', 'CASE_STUDY')),
    title VARCHAR(200) NOT NULL,
    description VARCHAR(1000),
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'PUBLISHED')),
    display_order INT NOT NULL DEFAULT 0,
    content_version INT NOT NULL DEFAULT 1,
    object_key VARCHAR(500) UNIQUE,
    file_name VARCHAR(255),
    file_size BIGINT,
    mime_type VARCHAR(100),
    alt_text VARCHAR(250),
    logo_object_key VARCHAR(500) UNIQUE,
    logo_file_name VARCHAR(255),
    logo_file_size BIGINT,
    logo_mime_type VARCHAR(100),
    video_provider VARCHAR(20) CHECK (video_provider IN ('YOUTUBE', 'VIMEO', 'EXTERNAL')),
    video_url VARCHAR(1000),
    video_ref VARCHAR(50),
    thumbnail_url VARCHAR(1000),
    customer_name VARCHAR(200),
    problem TEXT,
    result_text TEXT,
    knowledge_content_id BIGINT REFERENCES knowledge_article(id) ON DELETE SET NULL,
    published_at TIMESTAMP,
    created_by_sub VARCHAR(100),
    updated_by_sub VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_product_content_product ON product_content_item (product_id, status, display_order);
