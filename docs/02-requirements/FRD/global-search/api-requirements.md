# API requirements — Global Search

| Method | Path | Purpose | Permission | Success | Errors |
|--------|------|---------|------------|---------|--------|
| GET | `/search` | Unified search (BR-PRT-001–005, BR-SRCH-001–010; semantic: REQ-PRT-003) | Public (tickets only for a resolvable caller — BR-PRT-003) | 200 `GlobalSearchResultDto` | - |
| GET | `/search/suggest` | Type-ahead: up to 8 visible records whose title contains, or nearly matches, `q` (2+ letters) | Public | 200 `SearchSuggestionDto[]` | - |
| GET | `/me/search-history` · POST · DELETE | Recent searches (existing, Phase 17) | Signed in | 200 / 204 | 401 |
| GET | `/admin/search/index` | Index state, embedding model state, settings in use | `MANAGE_SEARCH` | 200 `SearchIndexStatusDto` | 401, 403 |
| POST | `/admin/search/index/rebuild?reembed=` | Rebuild the index in the background (`reembed=true`: embed every passage again) | `MANAGE_SEARCH` | 202 | 401, 403, 409 already running or V020 not applied |
| GET | `/admin/search/synonyms` | Synonym groups | `MANAGE_SEARCH` | 200 `SynonymDto[]` | 401, 403 |
| POST | `/admin/search/synonyms` | Add a group `{ "terms": ["invoice", "bill"] }` (2–10 terms, 1–50 characters) | `MANAGE_SEARCH` | 201 `SynonymDto` | 400, 401, 403 |
| DELETE | `/admin/search/synonyms/{id}` | Delete a group | `MANAGE_SEARCH` | 204 | 401, 403, 404 |
| GET | `/admin/search/insights?days=30` | Search insights for 1–365 days | `MANAGE_SEARCH` | 200 `SearchInsightsDto` | 401, 403 |

## `GET /search` parameters
| Parameter | Values | Default |
|---|---|---|
| `q` | Query text (first 200 characters, first 12 words used) | blank = everything visible |
| `type` | `PRODUCT`, `KNOWLEDGE`, `TICKET` (case-insensitive) | all |
| `mode` | `hybrid` (keyword + semantic), `keyword` | `hybrid` |
| `limit` | 1–100 | 50 |
| `track` | `true` = count in search insights | `false` |

## Response
```json
// GET /search?q=paying%20invoices
{
  "products": [],
  "knowledgeArticles": [{ "type": "KNOWLEDGE", "id": 7, "title": "How to pay an invoice", "...": "..." }],
  "tickets": [],
  "results": [
    {
      "type": "KNOWLEDGE", "id": 7, "title": "How to pay an invoice",
      "snippet": "Open Billing, choose the invoice and select Pay now…",
      "reference": "#7", "matchType": "KEYWORD", "score": 0.016393,
      "titleHighlights": [{ "start": 7, "length": 3 }, { "start": 14, "length": 7 }],
      "snippetHighlights": [{ "start": 23, "length": 7 }]
    }
  ],
  "didYouMean": null,
  "semanticStatus": "USED",
  "engine": "POSTGRES",
  "tookMs": 18
}
```
- `matchType`: `EXACT_ID`, `EXACT_PHRASE`, `KEYWORD`, `PARTIAL`, `TYPO`, `SEMANTIC`.
- `semanticStatus`: `USED`, `UNAVAILABLE` (model did not answer in time — keyword results only), `DISABLED` (not configured or pgvector missing), `NOT_APPLICABLE` (keyword mode, blank query, quoted phrase, exact ID, or type `TICKET`).
- `engine`: `POSTGRES` (search index) or `BASIC` (BR-SRCH-010).
- The grouped lists are kept for callers built before C70.

Full API reference: [`docs/06-api/api-requirements/search.md`](../../../06-api/api-requirements/search.md). OpenAPI contract: not maintained separately — the controllers, DTOs and this file are the source of truth ([C14](../../../01-business/roadmap/open-decisions.md#c14)).
