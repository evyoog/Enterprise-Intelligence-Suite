# REQ-KNW-001 — Knowledge Base

**Status:** Approved
**BRD:** Not specified
**Owner:** Product owner
**Approved by / on:** Product owner / 2026-09-28 ([C39](../../../01-business/roadmap/open-decisions.md#c39))

| Field | Value |
|---|---|
| Sprint | [2027.1.1](../../../01-business/roadmap/sprints/SPRINT-2027.1.1.md) |
| Requirement ID | REQ-KNW-001 |
| Application | [11 Training & Knowledge Management](../../../01-business/roadmap/applications/11-training-knowledge-management.md) |
| Application code | `APP-KNW` |
| Priority | P0 ([C4](../../../01-business/roadmap/open-decisions.md#c4), [C5](../../../01-business/roadmap/open-decisions.md#c5)) |
| AI required | No ([C12](../../../01-business/roadmap/open-decisions.md#c12)) — see Out of scope |

## Source functions
| Function ID | Function | Application page |
|---|---|---|
| 11.01.01 | Create article; Edit article; Publish article; Search article; Version article | [11 Training & Knowledge Management](../../../01-business/roadmap/applications/11-training-knowledge-management.md#1101-knowledge-base) |

11.01.02 AI Knowledge (index/retrieve/validate against an embedding store) is **not** covered by this requirement — see [C39](../../../01-business/roadmap/open-decisions.md#c39).

## Summary
A platform-admin-managed set of knowledge articles (title + body), each with a plain draft/published toggle and a revision counter. Published articles are searchable and readable by anyone, signed in or not — the same public-read model as the product catalog.

## Actors
- Platform administrator (`MANAGE_KNOWLEDGE_BASE`) — create/edit/publish/unpublish/delete
- Any visitor (signed in or not) — search and read published articles

## Functional requirements
| ID | Requirement | Priority |
|---|---|---|
| REQ-KNW-001.1 | An admin can create an article (title, body); it starts as DRAFT. | Must |
| REQ-KNW-001.2 | An admin can edit an article's title/body, which increments its version counter. | Must |
| REQ-KNW-001.3 | An admin can publish a DRAFT article (PUBLISHED) and unpublish a PUBLISHED one (back to DRAFT) — reversible either way. | Must |
| REQ-KNW-001.4 | An admin can delete an article. | Should |
| REQ-KNW-001.5 | Anyone can search PUBLISHED articles by a case-insensitive match on title or body; a blank query returns every published article. | Must |
| REQ-KNW-001.6 | Anyone can read one PUBLISHED article by id; a DRAFT article's id returns the same generic 404 a nonexistent id would, never confirming unpublished content exists. | Must |

## Out of scope
- 11.01.02 AI Knowledge: semantic indexing/retrieval against an embedding store, and "validate source" — no vector store or embeddings-model decision exists anywhere in this codebase yet (see C39); plain text search already satisfies "Search article".
- Any rich-text/markdown rendering, attachments, or categories/tags — title + plain body text only.
- Full version history (only a revision counter, mirroring `Product#version`).

## Dependencies
- New table `knowledge_article` ([V009](../../../../database/migrations/V009__order_lifecycle_knowledge_base.sql)).
- New permission `MANAGE_KNOWLEDGE_BASE` (platform ADMIN).
