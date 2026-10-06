# Data model — Knowledge Center

**Status:** Built 2026-10-05 ([C78](../../01-business/roadmap/open-decisions.md#c78)) for REQ-KNW-001.7 and REQ-KNW-002–008. Migration [`V021__knowledge_center.sql`](../../../database/migrations/V021__knowledge_center.sql), mirrored in `backend/src/main/resources/db/schema.sql`. All changes are **additive**; existing `knowledge_article` rows keep their ids, title, body, status and version (tested on a copy of the old schema, twice — idempotent).

Design choices made while building (differences from the Draft): JSON is stored as `TEXT` (blocks, type fields, chapters, subtitles) so the same entities run on the H2 test database; tags and related content are comma-separated columns rather than join tables; audiences are an enum plus an organization id list; readers are served from an immutable **version snapshot**, so a draft can be edited while the published version stays live.

## knowledge_article — the content table (extended)
| Column | Type | Notes |
|---|---|---|
| id, title, body, status, version, created_at, updated_at | (existing) | `body` = plain text of the blocks; `status` = PUBLISHED while a live version exists (old endpoints) |
| content_type | VARCHAR(30) default `ARTICLE` | ARTICLE … DEVELOPER_DOC, COURSE (reserved for the Academy) |
| slug | VARCHAR(220) | unique per type (checked by the service) |
| short_description | VARCHAR(500) | |
| blocks, type_fields | TEXT (JSON) | validated server side (BR-KCON-008) |
| product_id, module_id, category_id | BIGINT | taxonomy tables below |
| feature, tags, keywords | VARCHAR | tags comma-separated |
| audience | VARCHAR(20) default `PUBLIC` | PUBLIC, CUSTOMER, ORGANIZATION, ADMIN (BR-KVS-001) |
| audience_org_ids | VARCHAR(1000) | ",3,7," for ORGANIZATION |
| require_product_access | BOOLEAN | only readers with product access to the product |
| workflow_state | VARCHAR(20) default `DRAFT` | DRAFT, IN_REVIEW, APPROVED, SCHEDULED, PUBLISHED, DEPRECATED, ARCHIVED |
| live_version_id | BIGINT | the version readers see; null = not published |
| current_version_label | VARCHAR(10) default `0.1` | label of the live version |
| product_version, documentation_version | VARCHAR(50) | |
| effective_at, review_at, expires_at, scheduled_at, published_at, deprecated_at | TIMESTAMP | |
| scheduled_bump | VARCHAR(10) | MINOR/MAJOR for a scheduled publish |
| author_sub, reviewer_sub, approver_sub, updated_by_sub | VARCHAR(100) | Keycloak subjects |
| review_comment | VARCHAR(1000) | comment when returned to draft |
| featured | BOOLEAN | |
| difficulty | VARCHAR(20) | BEGINNER, INTERMEDIATE, ADVANCED |
| direct_action_route, direct_action_label | VARCHAR | EIS route only |
| related_content_ids | VARCHAR(1000) | ",12,15," |
| search_text | TEXT | type fields, taxonomy names, transcript, chapters — for search only |

## New tables
| Table | Purpose |
|---|---|
| knowledge_content_version | Immutable published snapshot: title, short description, blocks, type fields, body, search text, audience, organization ids, product-access flag, effective and expiry dates, published by/at; unique (content_id, version_label) |
| knowledge_product | Knowledge product: name, unique slug, description, `catalog_product_id` → products, order, active (seed: six products) |
| knowledge_module | Module per product, unique (product_id, slug) (seed: the prompt's lists) |
| knowledge_category | Category with optional `scope` (content type) (seed: troubleshooting, video and document categories) |
| knowledge_media | Private S3 object metadata: kind, provider, bucket, unique object key, pending key (replacement), file name, size, MIME type, ETag, status PENDING/READY/FAILED/CANCELLED/DELETED, version, product, module, multipart upload id, uploader, times. **No file and no URL is stored** |
| knowledge_video | One per VIDEO item: source YOUTUBE/AWS_S3/EXTERNAL_URL, video URL, YouTube id, media id (S3), thumbnail media id or provider URL, duration, channel, transcript, chapters JSON, subtitles JSON (language → media id) |
| knowledge_feedback | Vote / outdated / suggestion per content version; voter subject so a second vote replaces the first |
| knowledge_bookmark | Per customer (unique customer + content) |
| knowledge_progress | Per customer: percent, position, completed, last viewed (recently viewed and continue learning) |
| knowledge_event | Analytics: type, content, version, type, product, module, video source, organization id, percent, seconds, one-way viewer hash, time |
| knowledge_course, knowledge_lesson | Academy (11b) — **prepared only**, not used until confirmed (C77) |

`search_query_log` gains `scope` (null = global search, `KNOWLEDGE` = Knowledge Center) for gap detection.

## Search
`search_document` (C70) holds knowledge items of **every type** whose live version a signed-out visitor may see (Public, within dates, not product-restricted): title, body plus search text (so transcripts and chapters are searchable), keywords = type, tags, keywords. Restricted content is never in the shared index or its pgvector passages; the Knowledge Center searches it only among items the reader may already see. A scheduled job re-indexes items whose effective or expiry time passes.
