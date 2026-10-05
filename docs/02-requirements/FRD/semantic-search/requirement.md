# REQ-PRT-003 — Semantic Search

**Status:** Approved
**BRD:** Not specified
**Owner:** Product owner
**Approved by / on:** Product owner / 2026-10-05 ([C70](../../../01-business/roadmap/open-decisions.md#c70))

| Field | Value |
|---|---|
| Sprint | [2027.1.3](../../../01-business/roadmap/sprints/SPRINT-2027.1.3.md) (planned; built early on 2026-10-05) |
| Requirement ID | REQ-PRT-003 |
| Application | [01 Enterprise Intelligence Suite](../../../01-business/roadmap/applications/01-enterprise-intelligence-suite.md) |
| Application code | `APP-PRT` |
| Priority | P0 ([C4](../../../01-business/roadmap/open-decisions.md#c4), [C5](../../../01-business/roadmap/open-decisions.md#c5)) |
| AI required | Yes — an embedding model; no generated text ([C70](../../../01-business/roadmap/open-decisions.md#c70)) |

## Source functions
| Function ID | Function | Application page |
|---|---|---|
| 01.03.01.02 | Semantic search | [01 Enterprise Intelligence Suite](../../../01-business/roadmap/applications/01-enterprise-intelligence-suite.md#0103-global-search) |

## Summary
Global Search (REQ-PRT-002) also finds public content that is about the same topic as the query without using its words. Product and documentation text is split into passages, each passage is turned into a vector by an open-source multilingual embedding model (English and Spanish) running in `ai-service`, and the vectors are stored with pgvector. A search compares the query's vector with the passages and merges these matches with the keyword results (hybrid search). If the model is not available, search returns keyword results only.

## Decisions (C70, product owner, 2026-10-05)
- Hybrid search with pgvector (as C58).
- Embedding model: option B — an open-source multilingual model (English + Spanish) in the existing `ai-service`. **Which model: Not specified.** The index expects 384-dimensional vectors.
- Semantic search covers **public content only**: the product catalog and documentation (published knowledge articles). Support tickets use keyword search only.
- Chunk size and similarity threshold are tuned with the test set and documented ([quality report](quality-report.md)).
- No AI-written answers above the results (later).

## Actors
- Any visitor (signed in or not).
- Platform admin (`MANAGE_SEARCH`): sees the model's state, rebuilds and re-embeds.

## Functional requirements
| ID | Requirement | Priority |
|---|---|---|
| REQ-PRT-003.1 | `ai-service` offers `POST /embed` (texts → vectors) and `GET /embed/info` (model state). The model and provider are set in `config/secrets.env`. | Must |
| REQ-PRT-003.2 | Every public record (ACTIVE product, PUBLISHED article) is split into overlapping passages, each starting with the record's title, and each passage is embedded. Private records are never split or embedded. | Must |
| REQ-PRT-003.3 | Passages are kept in step with their record (REQ-PRT-002.5): changed text is embedded again; unchanged text keeps its vector. Passages waiting for the model are retried every minute. | Must |
| REQ-PRT-003.4 | In hybrid mode (the default), the query is embedded and compared with the passages of public records (cosine similarity, pgvector HNSW index); a record counts once, with its best passage, when that passage reaches the similarity threshold. | Must |
| REQ-PRT-003.5 | Keyword and semantic matches are merged into one ranking (reciprocal rank fusion); exact-ID matches stay first; a record found only by meaning is labelled "Similar meaning" and shows the passage that matched. | Must |
| REQ-PRT-003.6 | If the model does not answer within the timeout, answers with an error, or is not configured, the search returns the keyword results and says so (`semanticStatus`). | Must |
| REQ-PRT-003.7 | The admin Search page shows the model's state and the passage counts, and can rebuild with re-embedding (after a model change). | Must |
| REQ-PRT-003.8 | Search quality is measured on a test set of 30+ English and Spanish queries, before and after, and documented with the chosen values. | Must |

## Out of scope
- AI-written answers or summaries above the results (C70: later).
- Semantic search on tickets or any private record (C70).
- Choosing the model: **Not specified**; any 384-dimensional sentence-transformers model can be configured.
- Languages other than English and Spanish: **Not specified**.
- 11.01.02 AI Knowledge (index/retrieve/validate for an AI agent) — it can reuse these passages, but is its own feature.

## Dependencies
- PostgreSQL extension `pgvector` (0.5 or later, for HNSW). Without it, keyword search works fully and semantic search is off (`DISABLED`).
- `ai-service` with `sentence-transformers` (`requirements-embeddings.txt`) and a downloaded model for real use; the `stub` provider is for tests only.
- REQ-PRT-002 search index.

## Built with (2026-10-05)
- No real model could be downloaded in the build environment (huggingface.co blocked), so every semantic test and the quality report use the **stub** test model (word hashing). The pipeline, fallback and speed are verified; **the quality a real model gives is not measured**, and the similarity threshold and chunk size must be re-tuned once a model is chosen.
