-- V023: optional semantic search (REQ-PRT-003, C70). Needs the pgvector extension,
-- which on RDS must be created by an rds_superuser (CREATE EXTENSION vector).
-- Without it the backend detects the missing search_chunk table and uses keyword search only.
-- Idempotent. Run: psql -v ON_ERROR_STOP=1 -f V023__semantic_search_pgvector.sql
SET search_path TO eis_platform, public;
BEGIN;
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
COMMIT;
