-- C70 (2026-10-05): search index for keyword search (REQ-PRT-002) and
-- semantic search (REQ-PRT-003). Mirrors backend/src/main/resources/db/schema.sql
-- (Flyway is disabled; apply by hand). Part 2 needs pgvector; skip it where
-- pgvector is not installed and the backend runs keyword search only.
-- After applying, the backend fills the index on its next start (backfill).

SET search_path TO eis_platform, public;

-- REQ-PRT-002 / REQ-PRT-003 (C70, 2026-10-05): search index.
-- Part 1 — keyword search (needs pg_trgm, part of PostgreSQL contrib).
-- One row per searchable record. *_folded columns hold lower-case text with
-- accents removed (done by the backend), so "facturacion" finds "facturación".
-- visibility PUBLIC = anyone (catalog, published articles); OWNER = only
-- owner_customer_id (support tickets). Results are filtered on these two
-- columns before they are ranked.
CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE TABLE IF NOT EXISTS search_document (
    id BIGSERIAL PRIMARY KEY,
    source_type VARCHAR(20) NOT NULL CHECK (source_type IN ('PRODUCT', 'KNOWLEDGE', 'TICKET')),
    source_id BIGINT NOT NULL,
    visibility VARCHAR(10) NOT NULL CHECK (visibility IN ('PUBLIC', 'OWNER')),
    owner_customer_id BIGINT,
    reference VARCHAR(40) NOT NULL,
    title VARCHAR(300) NOT NULL,
    body TEXT,
    keywords VARCHAR(2000),
    title_folded VARCHAR(300) NOT NULL,
    body_folded TEXT,
    keywords_folded VARCHAR(2000),
    content_updated_at TIMESTAMP,
    indexed_at TIMESTAMP NOT NULL DEFAULT now(),
    tsv tsvector GENERATED ALWAYS AS (
        setweight(to_tsvector('simple', coalesce(title_folded, '')), 'A') ||
        setweight(to_tsvector('english', coalesce(title_folded, '')), 'A') ||
        setweight(to_tsvector('spanish', coalesce(title_folded, '')), 'A') ||
        setweight(to_tsvector('simple', coalesce(keywords_folded, '')), 'B') ||
        setweight(to_tsvector('english', coalesce(keywords_folded, '')), 'B') ||
        setweight(to_tsvector('spanish', coalesce(keywords_folded, '')), 'B') ||
        setweight(to_tsvector('simple', coalesce(body_folded, '')), 'D') ||
        setweight(to_tsvector('english', coalesce(body_folded, '')), 'C') ||
        setweight(to_tsvector('spanish', coalesce(body_folded, '')), 'C')
    ) STORED,
    CONSTRAINT uq_search_document_source UNIQUE (source_type, source_id),
    CONSTRAINT chk_search_document_owner CHECK (visibility = 'PUBLIC' OR owner_customer_id IS NOT NULL)
);
CREATE INDEX IF NOT EXISTS idx_search_document_tsv ON search_document USING GIN (tsv);
CREATE INDEX IF NOT EXISTS idx_search_document_title_trgm ON search_document USING GIN (title_folded gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_search_document_visibility ON search_document (visibility, owner_customer_id);
CREATE INDEX IF NOT EXISTS idx_search_document_reference ON search_document (reference);

-- Words of PUBLIC documents only, for "Did you mean". Private ticket words
-- are never added, so a suggestion can never reveal another user's ticket.
CREATE TABLE IF NOT EXISTS search_term (
    term VARCHAR(100) PRIMARY KEY,
    doc_count INT NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_search_term_trgm ON search_term USING GIN (term gin_trgm_ops);

-- Admin-managed synonym groups: comma-separated equivalent terms.
CREATE TABLE IF NOT EXISTS search_synonym (
    id BIGSERIAL PRIMARY KEY,
    terms VARCHAR(500) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

-- Search insights: one row per search run from the results page. No user
-- identity is stored.
CREATE TABLE IF NOT EXISTS search_query_log (
    id BIGSERIAL PRIMARY KEY,
    query VARCHAR(200) NOT NULL,
    result_count INT NOT NULL,
    mode VARCHAR(10) NOT NULL,
    semantic_used BOOLEAN NOT NULL DEFAULT FALSE,
    took_ms INT NOT NULL,
    searched_at TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_search_query_log_searched_at ON search_query_log (searched_at);

-- Admin "Rebuild index" runs.
CREATE TABLE IF NOT EXISTS search_index_run (
    id BIGSERIAL PRIMARY KEY,
    trigger_type VARCHAR(10) NOT NULL CHECK (trigger_type IN ('BACKFILL', 'ADMIN')),
    status VARCHAR(10) NOT NULL CHECK (status IN ('RUNNING', 'DONE', 'FAILED')),
    documents INT NOT NULL DEFAULT 0,
    chunks INT NOT NULL DEFAULT 0,
    embedded INT NOT NULL DEFAULT 0,
    error VARCHAR(1000),
    started_at TIMESTAMP NOT NULL DEFAULT now(),
    finished_at TIMESTAMP
);

-- Part 2 — semantic search (needs the pgvector extension). If pgvector is
-- not available, skip this part: the backend detects the missing table and
-- runs keyword search only. Only PUBLIC documents get chunks/embeddings.
-- vector(384): the dimension of the embedding model (C70).
CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE IF NOT EXISTS search_chunk (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT NOT NULL REFERENCES search_document(id) ON DELETE CASCADE,
    chunk_index INT NOT NULL,
    content TEXT NOT NULL,
    content_hash VARCHAR(64) NOT NULL,
    embedding vector(384),
    embedded_at TIMESTAMP,
    CONSTRAINT uq_search_chunk UNIQUE (document_id, chunk_index)
);
CREATE INDEX IF NOT EXISTS idx_search_chunk_embedding ON search_chunk USING hnsw (embedding vector_cosine_ops);
CREATE INDEX IF NOT EXISTS idx_search_chunk_pending ON search_chunk (document_id) WHERE embedding IS NULL;
