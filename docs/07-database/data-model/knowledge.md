# Data model — Knowledge Center (Draft)

**Status:** Draft (2026-10-05) for REQ-KNW-001.7 and REQ-KNW-002–008. Not built. All changes **additive**; one migration per build phase (V021+), mirrored in `schema.sql`. Existing `knowledge_article` rows keep their ids.

## Extended: knowledge_article → the content table
`knowledge_article` stays the content table (ids, links and old endpoints unchanged). New columns (nullable or with defaults that keep current behaviour):
| Column | Type | Default for existing rows |
|---|---|---|
| content_type | VARCHAR(30) | `ARTICLE` |
| slug | VARCHAR(200) UNIQUE per type | generated from title + id |
| short_description | VARCHAR(500) | NULL |
| blocks | JSONB | one paragraph block holding `body` |
| product_id, module_id, category_id | BIGINT FK knowledge taxonomy | NULL |
| feature | VARCHAR(200) | NULL |
| keywords | VARCHAR(1000) | NULL |
| type_fields | JSONB (FAQ, error code, release note… fields) | NULL |
| audience | JSONB (`{ kind, organizationIds[], roles[], groupIds[], productIds[], requireProductAccess }`) | `{ "kind": "PUBLIC" }` |
| workflow_state | VARCHAR(20) DRAFT/IN_REVIEW/APPROVED/SCHEDULED/PUBLISHED/DEPRECATED/ARCHIVED | from `status` |
| current_version_label | VARCHAR(10) (1.0, 1.1) | `version` + ".0" |
| product_version, documentation_version | VARCHAR(50) | NULL |
| effective_at, review_at, expires_at, scheduled_at, published_at | TIMESTAMP | `published_at` = `updated_at` for PUBLISHED |
| author_id, reviewer_id, approver_id, updated_by | BIGINT (customer) / VARCHAR (keycloak sub) | NULL |
| featured | BOOLEAN | false |
`status` (DRAFT/PUBLISHED) is kept in step with `workflow_state` for the old endpoints; `body` is kept as plain text of the blocks for the old endpoints and search.

## New tables
| Table | Purpose |
|---|---|
| knowledge_content_version | Immutable snapshot per published version: content_id, version_label, blocks, metadata JSONB, published_by, published_at |
| knowledge_product | Knowledge product: catalog `products.id` FK, slug, display order, active |
| knowledge_module | Module per knowledge product: name, slug, order, active (seed: the prompt's lists) |
| knowledge_category | Categories (incl. troubleshooting categories, video kinds), type scope, active |
| knowledge_tag, knowledge_content_tag | Tags |
| knowledge_relation | content_id, related_content_id or related product, kind (RELATED, GLOSSARY, DIRECT_ACTION route) |
| knowledge_media | id, kind (IMAGE/DOCUMENT/PDF/AUDIO/TEMPLATE/VIDEO_FILE/THUMBNAIL/TRANSCRIPT/SUBTITLE), storage_provider, s3_bucket, s3_object_key (unique), file_name, file_size, mime_type, checksum/etag, status (PENDING/READY/FAILED/DELETED), version, replaced_by, uploaded_by, uploaded_at |
| knowledge_media_usage | media_id, content_id, block id (for "used by") |
| knowledge_video | content_id (type VIDEO), video_source_type, video_url, video_id, media_id (S3), thumbnail media/url, duration_seconds, transcript (TEXT), transcript_cues JSONB, chapters JSONB, subtitles (language → media id), difficulty, channel |
| knowledge_workflow_step | workflow content_id, order, title, content_id link, EIS route |
| knowledge_feedback | content_id, version, customer_id (nullable), helpful, reason, comment, kind (VOTE/OUTDATED/SUGGESTION), created_at |
| knowledge_bookmark | customer_id, content_id, created_at (unique pair) |
| knowledge_progress | customer_id, content_id, position_seconds / percent, completed_at |
| knowledge_event | event type, content_id, version, content_type, product_id, module_id, source_type, organization_id, customer_id (open question), seconds, created_at |
| knowledge_course, knowledge_lesson, knowledge_quiz, knowledge_certificate | Academy — prepared only unless confirmed |

Search: `search_document.source_type` gains the knowledge types; `search_chunk` passages for public knowledge (including transcripts); the visibility filter adds the audience (BR-KVS-001). Indexes: `(content_type, workflow_state)`, GIN on `audience`, `(product_id, module_id)`, `knowledge_event (content_id, created_at)`.

Seed data (taxonomy, glossary terms, template list, workflow guides): data rows only, editable by publishers (C74).
