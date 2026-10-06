# API requirements — Semantic Search

## Backend
Semantic search has no endpoint of its own: `GET /search` (`mode=hybrid`, the default) includes it and reports `semanticStatus`; `GET /admin/search/index` reports the model and passages; `POST /admin/search/index/rebuild?reembed=true` re-embeds. See [Global Search API](../global-search/api-requirements.md).

## ai-service (internal, called by the backend only)
| Method | Path | Purpose | Success | Errors |
|--------|------|---------|---------|--------|
| GET | `/embed/info` | Model state | 200 `{ "enabled": true, "provider": "sentence-transformers", "model": "<name>", "dimension": 384, "message": null }` | - |
| POST | `/embed` | `{ "texts": ["…"], "kind": "query" \| "passage" }` (1–256 texts, each cut at 8,000 characters) | 200 `{ "provider", "model", "dimension", "vectors": [[…]] }` — L2-normalised | 422 invalid body; 503 no model loaded |

## Configuration (`config/secrets.env` only — C70 rule)
| Variable | Used by | Meaning |
|---|---|---|
| `EMBEDDING_SERVICE_URL` | backend | ai-service address, for example `http://localhost:8000`. Empty = keyword search only |
| `EMBEDDING_PROVIDER` | ai-service | `sentence-transformers` or `stub` (tests only). Empty = `sentence-transformers` when a model is set, otherwise off |
| `EMBEDDING_MODEL` | ai-service | Model id or local path: **Not specified** (must produce 384 dimensions) |
| `EMBEDDING_QUERY_PREFIX`, `EMBEDDING_PASSAGE_PREFIX` | ai-service | Text some models expect before queries and passages (for example "query: "); empty by default |
