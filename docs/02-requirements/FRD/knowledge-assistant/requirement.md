# REQ-KNW-007 — Knowledge assistant (prepared, not built)

**Status:** Approved (2026-10-05, [C78](../../../01-business/roadmap/open-decisions.md#c78)) — built early on 2026-10-05
**Owner:** Product owner
**Decision:** [C75](../../../01-business/roadmap/open-decisions.md#c75)

| Field | Value |
|---|---|
| Sprint | [2027.1.3](../../../01-business/roadmap/sprints/SPRINT-2027.1.3.md) (with 04b / 12.02, dates unchanged); 11.01.02 AI Knowledge ([2027.1.1](../../../01-business/roadmap/sprints/SPRINT-2027.1.1.md)) |
| Requirement ID | REQ-KNW-007 |
| Application | [11](../../../01-business/roadmap/applications/11-training-knowledge-management.md) (11.01.02 AI Knowledge), [04](../../../01-business/roadmap/applications/04-ai-advisor-agent-platform.md), [12](../../../01-business/roadmap/applications/12-support-service-management.md) (12.02 AI Support) |
| Priority | P0 (interface only now) |

## Summary
An "Ask eVyoog Knowledge" panel and its API contract. Answers will be generated only from content the reader may see and will always show their sources. **Answer generation is not built until D8 (LLM provider) is decided**: the panel shows "Coming soon" and the endpoint answers "not configured".

## Functional requirements
| ID | Requirement | Priority |
|---|---|---|
| REQ-KNW-007.1 | Panel: question box, answer area, sources list, related guides, recommended action, **Open EIS page** link; opened from the Knowledge Center header, article support block and search results. | Must |
| REQ-KNW-007.2 | `POST /knowledge/assistant/ask` contract: request `{ question, context: { contentId?, productId?, moduleId? } }`; response `{ answer, sources: { articles[], videos[] }, relatedGuides[], recommendedAction?, eisPage? }`. Now answers **501 Not Implemented with code `ASSISTANT_NOT_CONFIGURED`** (proposed — confirm 501 vs 503). | Must |
| REQ-KNW-007.3 | Retrieval (when built) uses the search index (C76: hybrid search, pgvector passages) filtered by BR-KVS-001 before any text reaches the model. | Must (later) |
| REQ-KNW-007.4 | Every answer lists its sources; with no allowed source, the assistant says it does not know. | Must (later) |
| REQ-KNW-007.5 | `GET /knowledge/assistant/status` → `{ configured: false }` so the UI shows "Coming soon". | Must |

## Out of scope
- LLM provider, prompts, answer generation, conversation history (D8, C40/C41).

## Open questions
| # | Question | Blocks approval |
|---|---|---|
| 1 | D8: LLM provider. | Blocks the build of answers, not this FRD |
| 2 | Status code for "not configured": 501 (proposed) or 503. | No — confirm in review |


## Answers applied on 2026-10-05 (C78)
The product owner said "start develop the code" on 2026-10-05 without answering the open questions. The recommended answers were applied as defaults; each can still be changed.

- 1. Not built until D8.
- 2. 501 ASSISTANT_NOT_CONFIGURED.
