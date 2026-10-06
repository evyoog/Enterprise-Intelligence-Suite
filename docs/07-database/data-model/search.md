# Data model — Search index (C70)

[REQ-PRT-002](../../02-requirements/FRD/global-search/requirement.md), [REQ-PRT-003](../../02-requirements/FRD/semantic-search/requirement.md). Migration `database/migrations/V020__search_index.sql`; the same changes are at the end of `backend/src/main/resources/db/schema.sql`. Flyway is disabled: apply by hand. Part 1 needs `pg_trgm`; part 2 needs `pgvector` and can be skipped (the backend then runs keyword search only). After applying, the backend fills the index on its next start (backfill) or from **Rebuild index**.

## search_document
One row per searchable record. Written only by the backend's indexer.

| Column | Type | Notes |
|---|---|---|
| id | BIGSERIAL PK | |
| source_type | VARCHAR(20) NOT NULL | PRODUCT, KNOWLEDGE, TICKET |
| source_id | BIGINT NOT NULL | id in products / knowledge_article / support_ticket. UNIQUE (source_type, source_id) |
| visibility | VARCHAR(10) NOT NULL | PUBLIC or OWNER |
| owner_customer_id | BIGINT | Required for OWNER (ticket requester) |
| reference | VARCHAR(40) NOT NULL | "#<source_id>" — the exact ID users can type |
| title, body, keywords | VARCHAR(300) / TEXT / VARCHAR(2000) | Display text (keywords: category, tags, platforms, variant; tickets: category, status) |
| title_folded, body_folded, keywords_folded | | Lower case, no accents, punctuation as spaces |
| content_updated_at | TIMESTAMP | Source record's update time |
| indexed_at | TIMESTAMP NOT NULL | |
| tsv | tsvector GENERATED STORED | simple + english + spanish over the folded columns; weights A title, B keywords, C/D body |

Indexes: GIN (tsv); GIN trigram (title_folded); (visibility, owner_customer_id); (reference).

## search_chunk (pgvector)
Passages of PUBLIC documents only.

| Column | Type | Notes |
|---|---|---|
| id | BIGSERIAL PK | |
| document_id | BIGINT NOT NULL | FK search_document ON DELETE CASCADE |
| chunk_index | INT NOT NULL | UNIQUE (document_id, chunk_index) |
| content | TEXT NOT NULL | Title + passage |
| content_hash | VARCHAR(64) NOT NULL | SHA-256 of content |
| embedding | vector(384) | NULL = waiting for the model |
| embedded_at | TIMESTAMP | |

Indexes: HNSW (embedding vector_cosine_ops); partial index on pending rows.

## search_term
Words of PUBLIC documents with their document count, for "Did you mean" (GIN trigram on term). Rebuilt from `search_document`; never contains ticket words.

## search_synonym
| id BIGSERIAL PK | terms VARCHAR(500) NOT NULL (comma-separated, folded) | created_at TIMESTAMP NOT NULL |
|---|---|---|

## search_query_log
Searches run from the results page (`track=true`). No user identity.

| Column | Type |
|---|---|
| id | BIGSERIAL PK |
| query | VARCHAR(200) NOT NULL |
| result_count | INT NOT NULL |
| mode | VARCHAR(10) NOT NULL (HYBRID / KEYWORD) |
| semantic_used | BOOLEAN NOT NULL |
| took_ms | INT NOT NULL |
| searched_at | TIMESTAMP NOT NULL (indexed) |

Retention: **Not specified** (kept indefinitely).

## search_index_run
Full builds: trigger_type (BACKFILL / ADMIN), status (RUNNING / DONE / FAILED), documents, chunks, embedded, error, started_at, finished_at.

## Permission
`MANAGE_SEARCH` (platform) is added to the ADMIN role by `RbacSeeder` on startup; no SQL needed.
