# API — Search (C70)

Module `backend/src/main/java/com/vyoog/eisplatform/modules/search`. Requirements: [REQ-PRT-002](../../02-requirements/FRD/global-search/api-requirements.md), [REQ-PRT-003](../../02-requirements/FRD/semantic-search/api-requirements.md).

| Method | Path | Controller | Permission | Notes |
|---|---|---|---|---|
| GET | `/search` | `GlobalSearchController` | Public | `q`, `type`, `mode` (hybrid/keyword), `limit` (1–100), `track` |
| GET | `/search/suggest` | `GlobalSearchController` | Public | `q` (2+ letters), `type` |
| GET | `/admin/search/index` | `AdminSearchController` | `MANAGE_SEARCH` | Index and model state |
| POST | `/admin/search/index/rebuild` | `AdminSearchController` | `MANAGE_SEARCH` | `reembed`; 202; 409 when running or V020 missing |
| GET / POST | `/admin/search/synonyms` | `AdminSearchController` | `MANAGE_SEARCH` | Body `{ "terms": [...] }` |
| DELETE | `/admin/search/synonyms/{id}` | `AdminSearchController` | `MANAGE_SEARCH` | 404 unknown |
| GET | `/admin/search/insights` | `AdminSearchController` | `MANAGE_SEARCH` | `days` 1–365 |

Also available under `/v1/…` (REQ-INT-001). Visibility rule for every search query: BR-SRCH-001.

ai-service (internal): `GET /embed/info`, `POST /embed` — see the semantic-search API page.
