-- REQ-KNW-001 to REQ-KNW-008 (C71-C77, 2026-10-05): Knowledge Center and
-- Knowledge Management CMS. Additive only: existing knowledge articles keep
-- their ids, title, body, status and version (REQ-KNW-001.7, BR-KNW-007).
-- Mirrors backend/src/main/resources/db/schema.sql (Flyway is disabled;
-- apply by hand). Data model: docs/07-database/data-model/knowledge.md.

SET search_path TO eis_platform, public;

-- REQ-KNW-001.7 / REQ-KNW-002: knowledge_article becomes the content table
-- (additive; existing rows keep ids, title, body, status and version).
ALTER TABLE knowledge_article ADD COLUMN IF NOT EXISTS content_type VARCHAR(30) NOT NULL DEFAULT 'ARTICLE';
ALTER TABLE knowledge_article ADD COLUMN IF NOT EXISTS slug VARCHAR(220);
ALTER TABLE knowledge_article ADD COLUMN IF NOT EXISTS short_description VARCHAR(500);
ALTER TABLE knowledge_article ADD COLUMN IF NOT EXISTS blocks TEXT;
ALTER TABLE knowledge_article ADD COLUMN IF NOT EXISTS type_fields TEXT;
ALTER TABLE knowledge_article ADD COLUMN IF NOT EXISTS product_id BIGINT;
ALTER TABLE knowledge_article ADD COLUMN IF NOT EXISTS module_id BIGINT;
ALTER TABLE knowledge_article ADD COLUMN IF NOT EXISTS category_id BIGINT;
ALTER TABLE knowledge_article ADD COLUMN IF NOT EXISTS feature VARCHAR(200);
ALTER TABLE knowledge_article ADD COLUMN IF NOT EXISTS tags VARCHAR(1000);
ALTER TABLE knowledge_article ADD COLUMN IF NOT EXISTS keywords VARCHAR(1000);
ALTER TABLE knowledge_article ADD COLUMN IF NOT EXISTS audience VARCHAR(20) NOT NULL DEFAULT 'PUBLIC';
ALTER TABLE knowledge_article ADD COLUMN IF NOT EXISTS audience_org_ids VARCHAR(1000);
ALTER TABLE knowledge_article ADD COLUMN IF NOT EXISTS require_product_access BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE knowledge_article ADD COLUMN IF NOT EXISTS workflow_state VARCHAR(20) NOT NULL DEFAULT 'DRAFT';
ALTER TABLE knowledge_article ADD COLUMN IF NOT EXISTS live_version_id BIGINT;
ALTER TABLE knowledge_article ADD COLUMN IF NOT EXISTS current_version_label VARCHAR(10) NOT NULL DEFAULT '0.1';
ALTER TABLE knowledge_article ADD COLUMN IF NOT EXISTS product_version VARCHAR(50);
ALTER TABLE knowledge_article ADD COLUMN IF NOT EXISTS documentation_version VARCHAR(50);
ALTER TABLE knowledge_article ADD COLUMN IF NOT EXISTS effective_at TIMESTAMP;
ALTER TABLE knowledge_article ADD COLUMN IF NOT EXISTS review_at TIMESTAMP;
ALTER TABLE knowledge_article ADD COLUMN IF NOT EXISTS expires_at TIMESTAMP;
ALTER TABLE knowledge_article ADD COLUMN IF NOT EXISTS scheduled_at TIMESTAMP;
ALTER TABLE knowledge_article ADD COLUMN IF NOT EXISTS scheduled_bump VARCHAR(10);
ALTER TABLE knowledge_article ADD COLUMN IF NOT EXISTS published_at TIMESTAMP;
ALTER TABLE knowledge_article ADD COLUMN IF NOT EXISTS deprecated_at TIMESTAMP;
ALTER TABLE knowledge_article ADD COLUMN IF NOT EXISTS author_sub VARCHAR(100);
ALTER TABLE knowledge_article ADD COLUMN IF NOT EXISTS reviewer_sub VARCHAR(100);
ALTER TABLE knowledge_article ADD COLUMN IF NOT EXISTS approver_sub VARCHAR(100);
ALTER TABLE knowledge_article ADD COLUMN IF NOT EXISTS updated_by_sub VARCHAR(100);
ALTER TABLE knowledge_article ADD COLUMN IF NOT EXISTS review_comment VARCHAR(1000);
ALTER TABLE knowledge_article ADD COLUMN IF NOT EXISTS featured BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE knowledge_article ADD COLUMN IF NOT EXISTS difficulty VARCHAR(20);
ALTER TABLE knowledge_article ADD COLUMN IF NOT EXISTS direct_action_route VARCHAR(300);
ALTER TABLE knowledge_article ADD COLUMN IF NOT EXISTS direct_action_label VARCHAR(100);
ALTER TABLE knowledge_article ADD COLUMN IF NOT EXISTS related_content_ids VARCHAR(1000);
ALTER TABLE knowledge_article ADD COLUMN IF NOT EXISTS search_text TEXT;
CREATE INDEX IF NOT EXISTS idx_knowledge_article_type_state ON knowledge_article (content_type, workflow_state);
CREATE INDEX IF NOT EXISTS idx_knowledge_article_product_module ON knowledge_article (product_id, module_id);
CREATE INDEX IF NOT EXISTS idx_knowledge_article_slug ON knowledge_article (content_type, slug);

-- Immutable published versions (BR-KCON-004); readers see the live one.
CREATE TABLE IF NOT EXISTS knowledge_content_version (
    id BIGSERIAL PRIMARY KEY,
    content_id BIGINT NOT NULL REFERENCES knowledge_article(id) ON DELETE CASCADE,
    version_label VARCHAR(10) NOT NULL,
    title VARCHAR(200) NOT NULL,
    short_description VARCHAR(500),
    blocks TEXT,
    type_fields TEXT,
    body VARCHAR(20000) NOT NULL,
    search_text TEXT,
    audience VARCHAR(20) NOT NULL DEFAULT 'PUBLIC',
    audience_org_ids VARCHAR(1000),
    require_product_access BOOLEAN NOT NULL DEFAULT FALSE,
    effective_at TIMESTAMP,
    expires_at TIMESTAMP,
    published_by_sub VARCHAR(100),
    published_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uq_knowledge_content_version UNIQUE (content_id, version_label)
);

-- Taxonomy as data (C74).
CREATE TABLE IF NOT EXISTS knowledge_product (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    slug VARCHAR(120) NOT NULL UNIQUE,
    description VARCHAR(1000),
    catalog_product_id BIGINT REFERENCES products(id) ON DELETE SET NULL,
    display_order INT NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE
);
CREATE TABLE IF NOT EXISTS knowledge_module (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES knowledge_product(id),
    name VARCHAR(100) NOT NULL,
    slug VARCHAR(120) NOT NULL,
    display_order INT NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_knowledge_module_slug UNIQUE (product_id, slug)
);
CREATE TABLE IF NOT EXISTS knowledge_category (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    slug VARCHAR(120) NOT NULL,
    scope VARCHAR(30),
    display_order INT NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_knowledge_category_slug UNIQUE (scope, slug)
);

-- Media in the private S3 bucket: metadata only, never files or URLs (BR-MED-001).
CREATE TABLE IF NOT EXISTS knowledge_media (
    id BIGSERIAL PRIMARY KEY,
    kind VARCHAR(20) NOT NULL,
    storage_provider VARCHAR(20) NOT NULL,
    s3_bucket VARCHAR(100),
    s3_object_key VARCHAR(500) NOT NULL UNIQUE,
    pending_object_key VARCHAR(500),
    file_name VARCHAR(255) NOT NULL,
    file_size BIGINT NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    etag VARCHAR(100),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING'
        CHECK (status IN ('PENDING', 'READY', 'FAILED', 'CANCELLED', 'DELETED')),
    media_version INT NOT NULL DEFAULT 1,
    product_id BIGINT,
    module_id BIGINT,
    upload_id VARCHAR(300),
    uploaded_by_sub VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    uploaded_at TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_knowledge_media_status ON knowledge_media (status, created_at);

-- Video source and text tracks of VIDEO content (REQ-KNW-004, BR-KVID-001).
CREATE TABLE IF NOT EXISTS knowledge_video (
    id BIGSERIAL PRIMARY KEY,
    content_id BIGINT NOT NULL UNIQUE REFERENCES knowledge_article(id) ON DELETE CASCADE,
    video_source_type VARCHAR(20) NOT NULL CHECK (video_source_type IN ('YOUTUBE', 'AWS_S3', 'EXTERNAL_URL')),
    video_url VARCHAR(1000),
    video_id VARCHAR(20),
    media_id BIGINT REFERENCES knowledge_media(id),
    thumbnail_media_id BIGINT REFERENCES knowledge_media(id),
    thumbnail_url VARCHAR(1000),
    duration_seconds INT,
    channel VARCHAR(200),
    transcript TEXT,
    chapters TEXT,
    subtitles TEXT
);

-- Reader feedback, bookmarks, progress and analytics events (REQ-KNW-005/006).
CREATE TABLE IF NOT EXISTS knowledge_feedback (
    id BIGSERIAL PRIMARY KEY,
    content_id BIGINT NOT NULL REFERENCES knowledge_article(id) ON DELETE CASCADE,
    version_label VARCHAR(10),
    customer_id BIGINT,
    voter_sub VARCHAR(100),
    kind VARCHAR(20) NOT NULL CHECK (kind IN ('VOTE', 'OUTDATED', 'SUGGESTION')),
    helpful BOOLEAN,
    reason VARCHAR(40),
    comment_text VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_knowledge_feedback_content ON knowledge_feedback (content_id, created_at);
CREATE TABLE IF NOT EXISTS knowledge_bookmark (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES customer(id),
    content_id BIGINT NOT NULL REFERENCES knowledge_article(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uq_knowledge_bookmark UNIQUE (customer_id, content_id)
);
CREATE TABLE IF NOT EXISTS knowledge_progress (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES customer(id),
    content_id BIGINT NOT NULL REFERENCES knowledge_article(id) ON DELETE CASCADE,
    percent INT NOT NULL DEFAULT 0,
    position_seconds INT,
    completed_at TIMESTAMP,
    last_viewed_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uq_knowledge_progress UNIQUE (customer_id, content_id)
);
CREATE TABLE IF NOT EXISTS knowledge_event (
    id BIGSERIAL PRIMARY KEY,
    event_type VARCHAR(40) NOT NULL,
    content_id BIGINT NOT NULL,
    version_label VARCHAR(10),
    content_type VARCHAR(30),
    product_id BIGINT,
    module_id BIGINT,
    source_type VARCHAR(20),
    organization_id BIGINT,
    percent INT,
    seconds INT,
    viewer_hash VARCHAR(64),
    created_at TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_knowledge_event_content ON knowledge_event (content_id, created_at);
CREATE INDEX IF NOT EXISTS idx_knowledge_event_type ON knowledge_event (event_type, created_at);

-- Knowledge Center searches are logged with scope KNOWLEDGE (REQ-KNW-006.2).
ALTER TABLE search_query_log ADD COLUMN IF NOT EXISTS scope VARCHAR(20);
CREATE INDEX IF NOT EXISTS idx_search_query_log_scope ON search_query_log (scope, searched_at);

-- Academy (11b, REQ-KNW-005.22): prepared only, not used until confirmed (C77).
CREATE TABLE IF NOT EXISTS knowledge_course (
    id BIGSERIAL PRIMARY KEY,
    content_id BIGINT NOT NULL UNIQUE REFERENCES knowledge_article(id) ON DELETE CASCADE,
    certificate_name VARCHAR(200),
    pass_percent INT
);
CREATE TABLE IF NOT EXISTS knowledge_lesson (
    id BIGSERIAL PRIMARY KEY,
    course_id BIGINT NOT NULL REFERENCES knowledge_course(id) ON DELETE CASCADE,
    lesson_order INT NOT NULL,
    content_id BIGINT REFERENCES knowledge_article(id),
    title VARCHAR(200) NOT NULL
);

-- Part 2 — existing articles become ARTICLE items (REQ-KNW-001.7): one
-- paragraph block holding the body; published ones get a live version
-- "{version}.0" with a Public audience. The backend repeats this at startup
-- (KnowledgeSeeder), so it is safe to run either way.
UPDATE knowledge_article
   SET blocks = json_build_array(json_build_object('type', 'paragraph', 'text', body))::text
 WHERE blocks IS NULL;
UPDATE knowledge_article
   SET slug = trim(both '-' from regexp_replace(lower(title), '[^a-z0-9]+', '-', 'g')) || '-' || id
 WHERE slug IS NULL;
INSERT INTO knowledge_content_version (content_id, version_label, title, blocks, body, audience, published_at)
SELECT id, version || '.0', title, blocks, body, 'PUBLIC', updated_at
  FROM knowledge_article
 WHERE status = 'PUBLISHED' AND live_version_id IS NULL
ON CONFLICT (content_id, version_label) DO NOTHING;
UPDATE knowledge_article a
   SET live_version_id = v.id,
       current_version_label = v.version_label,
       workflow_state = 'PUBLISHED',
       published_at = v.published_at
  FROM knowledge_content_version v
 WHERE v.content_id = a.id AND a.status = 'PUBLISHED' AND a.live_version_id IS NULL
   AND v.version_label = a.version || '.0';
