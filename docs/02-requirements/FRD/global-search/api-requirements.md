# API requirements — Global Search

| Method | Path | Purpose | Permission | Success | Errors |
|--------|------|---------|------------|---------|--------|
| GET | `/search` | Unified keyword search (BR-PRT-001–.005) | Public (ticket results scoped to a resolvable caller — BR-PRT-003) | 200 (`GlobalSearchResultDto`) | - |

## Request / response
```json
// GET /search?q=login&type=TICKET
{
  "products": [],
  "knowledgeArticles": [],
  "tickets": [
    { "type": "TICKET", "id": 12, "title": "Can't log in", "snippet": "OPEN" }
  ]
}
```
```json
// GET /search?q=valam
{
  "products": [{ "type": "PRODUCT", "id": 3, "title": "Valam.ai", "snippet": "Analytics" }],
  "knowledgeArticles": [{ "type": "KNOWLEDGE", "id": 7, "title": "Getting started with Valam.ai", "snippet": "How to sign in to your account…" }],
  "tickets": []
}
```

OpenAPI contract: not maintained separately — `GlobalSearchController`/`GlobalSearchResultDto` and this file are the source of truth ([C14](../../../01-business/roadmap/open-decisions.md#c14)).
