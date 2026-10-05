# API — Knowledge Center and Knowledge Management (Draft)

**Status:** Draft (2026-10-05) for [REQ-KNW-002](../../02-requirements/FRD/knowledge-content/requirement.md)–[REQ-KNW-008](../../02-requirements/FRD/knowledge-permissions/requirement.md). Not built. Conventions as existing EIS APIs: context path `/api` (shown without it), `/v1/...` alias, errors `{ timestamp, status, error, message, code? }`, pages `{ items, totalElements, page, size }`.

Permissions: **R** reader (BR-KVS-001; Public allowed signed out if confirmed), **C** contributor (`KNOWLEDGE_CONTRIBUTE`), **P** publisher (`MANAGE_KNOWLEDGE_BASE`), **A** ADMIN (all). Admin endpoints live under `/admin/knowledge/**` (the prompt's `/api/knowledge/...` names adapted to the EIS reader/admin split).

## Reader
| Method | Path | Perm | Purpose |
|---|---|---|---|
| GET | `/knowledge/home` | R | Quick access counts, recommended, popular guides, featured videos, popular searches |
| GET | `/knowledge/content?type=&product=&module=&category=&tag=&page=&size=` | R | Browse lists (visible only) |
| GET | `/knowledge/content/{idOrSlug}` | R | One item with blocks (404 when not visible) |
| GET | `/knowledge/products`, `/knowledge/products/{slug}` | R | Product cards and hubs (modules, content counts) |
| GET | `/knowledge/workflows`, `/knowledge/error-codes/{code}`, `/knowledge/glossary`, `/knowledge/release-notes?product=` | R | Section data |
| GET | `/knowledge/videos/{id}/play-url` | R | `{ url, expiresAt }` for AWS_S3; `{ provider, embedId }` for YouTube; `{ url }` for external |
| GET | `/knowledge/media/{id}/download-url` | R | `{ url, expiresAt, fileName, size }` |
| POST | `/knowledge/content/{id}/feedback` | R | `{ helpful, reason?, comment? }`; also `report-outdated`, `suggest-improvement` |
| POST | `/knowledge/events` | R | Analytics events (REQ-KNW-006.1), batched |

Old endpoints kept (REQ-KNW-001.8): `GET /knowledge-base/articles`, `GET /knowledge-base/articles/{id}`, `/admin/knowledge-base/articles/**` (deprecated).

## Personal (signed in)
| Method | Path | Purpose |
|---|---|---|
| GET / PUT / DELETE | `/me/knowledge/bookmarks[/{contentId}]` | Bookmarks and saved items |
| GET | `/me/knowledge/recent` | Recently viewed |
| GET / PUT | `/me/knowledge/progress[/{contentId}]` | Continue where you left off, video position, course progress |

## Search
`GET /search?scope=knowledge&q=&type=` — the C70 endpoint with a `scope` and the new types (VIDEO, FAQ, TROUBLESHOOTING, ERROR_CODE, DOCUMENT, TEMPLATE, RELEASE_NOTE, GLOSSARY_TERM, WORKFLOW_GUIDE, DEVELOPER_DOC, MODULE…); results grouped by type. See [search.md](search.md).

## Content (admin)
| Method | Path | Perm | Purpose |
|---|---|---|---|
| GET | `/admin/knowledge/content?type=&status=&product=&q=&page=` | C | List |
| POST | `/admin/knowledge/content` | C | Create (Draft) |
| GET / PUT | `/admin/knowledge/content/{id}` | C | Read / update draft (blocks, metadata, audience) |
| DELETE | `/admin/knowledge/content/{id}` | P | Delete |
| POST | `/admin/knowledge/content/{id}/submit` | C | Draft → In review |
| POST | `/admin/knowledge/content/{id}/approve` · `/return` | P | Review decision (`{ comment }`) |
| POST | `/admin/knowledge/content/{id}/publish` | P | `{ versionBump: MINOR|MAJOR, scheduleAt? }` |
| POST | `/admin/knowledge/content/{id}/unpublish` · `/deprecate` · `/archive` · `/restore` | P | State changes |
| GET | `/admin/knowledge/content/{id}/versions`, `/versions/{v}`, `/versions/compare?from=&to=` | C | History and compare |
| POST | `/admin/knowledge/content/{id}/versions/{v}/restore` | P | Restore into a new draft |
| GET | `/admin/knowledge/content/{id}/preview?audience=` | C | Preview payload (same shape as the reader endpoint) |
| GET / POST / PUT / DELETE | `/admin/knowledge/taxonomy/{products|modules|categories}` | P (read C) | Taxonomy |

## Media
| Method | Path | Perm | Purpose |
|---|---|---|---|
| POST | `/admin/knowledge/media/upload-url` | C | `{ fileName, contentType, size, kind, productId, moduleId }` → `{ mediaId, uploadUrl, objectKey, expiresAt }` (or `{ multipart: { uploadId, partUrls[] } }`) |
| POST | `/admin/knowledge/media/{id}/complete-upload` | C | `{ objectKey, etag?, parts? }` → verified media item |
| POST | `/admin/knowledge/media/{id}/abort` | C | Cancel |
| GET | `/admin/knowledge/media?kind=&q=&page=` | C | Library (with used-by) |
| GET | `/admin/knowledge/media/{id}` | C | Detail incl. storage information (no credentials) |
| POST | `/admin/knowledge/media/{id}/replace` | P | New upload URL for a replacement → complete → new version |
| DELETE | `/admin/knowledge/media/{id}?confirm=true` | P | 409 with `usedBy` without `confirm` |

## Videos
| Method | Path | Perm | Purpose |
|---|---|---|---|
| POST | `/admin/knowledge/videos/upload-url` | C | As media, kind VIDEO (MP4, WebM, MOV) |
| POST | `/admin/knowledge/videos/{id}/complete-upload` | C | Verify (HEAD) |
| POST | `/admin/knowledge/videos` | C | Create any source `{ sourceType, youtubeUrl? | url? | mediaId?, title, productId, moduleId, categoryId, … }` |
| PUT / DELETE | `/admin/knowledge/videos/{id}` | C / P | Update / delete (deletes the S3 object) |
| POST | `/admin/knowledge/videos/{id}/replace` | P | Replace file |
| POST | `/admin/knowledge/videos/youtube/fetch-details` | C | `{ url }` → `{ videoId, title, description?, durationSeconds?, channel, thumbnailUrl, source: DATA_API|OEMBED }` |
| PUT | `/admin/knowledge/videos/{id}/transcript` · `/chapters` · `/subtitles/{lang}` | C | Text tracks |
| GET | `/admin/knowledge/videos/summary` | C | Total, published, draft, review, views |

## Search index (knowledge)
| Method | Path | Perm | Purpose |
|---|---|---|---|
| GET | `/admin/knowledge/search-index` | P | Indexed counts by type, embedding status, last indexing, failed items with reasons |
| POST | `/admin/knowledge/search-index/reindex` · `/reindex/{contentId}` | P | Re-index knowledge (full rebuild of all search stays `/admin/search/index/rebuild`, `MANAGE_SEARCH`) |

## Analytics
| GET | `/admin/knowledge/analytics?days=` | C | Views, downloads, searches, helpful %, popular, tickets, video metrics, gaps |
|---|---|---|---|
| GET | `/admin/knowledge/dashboard` | C | Counts by state and type, requires-review, gaps |

## Assistant
| GET | `/knowledge/assistant/status` | R | `{ configured: false }` |
|---|---|---|---|
| POST | `/knowledge/assistant/ask` | R | 501 `ASSISTANT_NOT_CONFIGURED` until D8 |

## Errors (friendly `message`, `code`)
`INVALID_FILE_TYPE`, `FILE_TOO_LARGE`, `UPLOAD_URL_EXPIRED`, `UPLOAD_NOT_FOUND` (HEAD failed), `UPLOAD_MISMATCH`, `STORAGE_UNAVAILABLE`, `DUPLICATE_UPLOAD`, `UPLOAD_CANCELLED`, `PERMISSION_DENIED` (403), `NOT_FOUND` (404, also for not-visible content), `INVALID_STATE` (409), `IN_USE` (409 with `usedBy`), `YOUTUBE_NOT_FOUND`, `YOUTUBE_UNAVAILABLE`. Stack traces and AWS errors are logged server-side only.
