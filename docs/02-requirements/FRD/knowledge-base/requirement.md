# REQ-KNW-001 — Knowledge Base

**Status:** Approved
**BRD:** Not specified
**Owner:** Product owner
**Approved by / on:** Product owner / 2026-09-28 ([C39](../../../01-business/roadmap/open-decisions.md#c39)) — the 2026-10-05 addition below (REQ-KNW-001.7–.11) approved 2026-10-05 ([C78](../../../01-business/roadmap/open-decisions.md#c78)) and built the same day

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

## Update 2026-10-05 — articles in the Knowledge Center content model (Approved and built 2026-10-05, [C78](../../../01-business/roadmap/open-decisions.md#c78); [C71](../../../01-business/roadmap/open-decisions.md#c71)–[C77](../../../01-business/roadmap/open-decisions.md#c77))

The Knowledge Center ([REQ-KNW-005](../knowledge-center/requirement.md)) and the content model ([REQ-KNW-002](../knowledge-content/requirement.md)) replace this feature's screens. Existing articles become one content type in the new model **with no loss of data or links**.

| ID | Requirement | Priority |
|---|---|---|
| REQ-KNW-001.7 | Every existing `knowledge_article` row keeps its id and becomes content type **ARTICLE** in the content model: its body becomes one paragraph block (text unchanged), its audience is **Public**, its workflow state maps DRAFT → Draft and PUBLISHED → Published, its version number is kept as the current version. The migration is additive; nothing is deleted. | Must |
| REQ-KNW-001.8 | The existing endpoints keep working with the same request and response shapes: `GET /knowledge-base/articles?q=`, `GET /knowledge-base/articles/{id}` (Public, Published content only) and `/admin/knowledge-base/articles/**` (now allowed for the knowledge permissions of [REQ-KNW-008](../knowledge-permissions/requirement.md)). They are marked deprecated in the API document once the new endpoints exist. | Must |
| REQ-KNW-001.9 | The `/knowledge-base` route redirects to the Knowledge Center; `/knowledge-base?article={id}` (if used) opens that article in the Knowledge Center. `/admin/knowledge-base` redirects to Knowledge Management → Articles. | Must |
| REQ-KNW-001.10 | Search keeps finding migrated articles (search index source type KNOWLEDGE, ids unchanged — REQ-PRT-002). | Must |
| REQ-KNW-001.11 | BR-KNW-001–006 keep holding for articles created through the old endpoints; articles created in the new editor follow REQ-KNW-002's workflow. | Must |

Open questions: none beyond those of REQ-KNW-002 and REQ-KNW-008.
