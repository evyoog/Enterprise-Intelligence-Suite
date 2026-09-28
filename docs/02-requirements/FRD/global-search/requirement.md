# REQ-PRT-002 — Global Search

**Status:** Approved
**BRD:** Not specified
**Owner:** Product owner
**Approved by / on:** Product owner / 2026-09-28 ([C41](../../../01-business/roadmap/open-decisions.md#c41))

| Field | Value |
|---|---|
| Sprint | [2027.1.3](../../../01-business/roadmap/sprints/SPRINT-2027.1.3.md) |
| Requirement ID | REQ-PRT-002 |
| Application | [01 Enterprise Intelligence Suite](../../../01-business/roadmap/applications/01-enterprise-intelligence-suite.md) |
| Application code | `APP-PRT` |
| Priority | P0 ([C4](../../../01-business/roadmap/open-decisions.md#c4), [C5](../../../01-business/roadmap/open-decisions.md#c5)) |
| AI required | No ([C12](../../../01-business/roadmap/open-decisions.md#c12)) — see Out of scope |

## Source functions
| Function ID | Function | Application page |
|---|---|---|
| 01.03.01 | Keyword search; Filter results; Sort results; View search history | [01 Enterprise Intelligence Suite](../../../01-business/roadmap/applications/01-enterprise-intelligence-suite.md#0103-global-search) |

01.03.01.02 Semantic search is **not** covered by this requirement — see [C41](../../../01-business/roadmap/open-decisions.md#c41). 01.02 Customer Dashboard's own "on live data" functions are already satisfied by the existing `BusinessDashboardService` (built in earlier phases) and are likewise not covered here.

## Summary
One search endpoint that combines three sources this platform already searches independently — the product catalog, published knowledge articles, and (signed in) the caller's own support tickets — into a single keyword result, filterable by type. Recording/showing search history reuses the existing per-customer search-history feature (Phase 17) exactly as the product catalog page already does.

## Actors
- Any visitor (signed in or not) — searches products and published knowledge articles
- Any authenticated customer — additionally sees their own matching tickets

## Functional requirements
| ID | Requirement | Priority |
|---|---|---|
| REQ-PRT-002.1 | `GET /search?q=` returns matching ACTIVE products, PUBLISHED knowledge articles, and (if the caller is signed in) the caller's own matching support tickets, grouped by type. | Must |
| REQ-PRT-002.2 | `type` narrows the result to exactly one of PRODUCT, KNOWLEDGE, or TICKET; omitting it returns all three. | Must |
| REQ-PRT-002.3 | A signed-out caller, or one with no linked Customer row, never sees ticket results, and never gets an error for it — the ticket list is simply empty. | Must |
| REQ-PRT-002.4 | Each result source keeps its own existing sort order (name for products, most-recently-updated for articles, most recent for tickets) — there is no cross-type relevance ranking. | Should |

## Out of scope
- Semantic/AI-based search (01.03.01.02) — no vector store or embeddings-model decision exists anywhere in this codebase (same gap as 11.01.02 AI Knowledge).
- Any new search-history storage or UI — reuses the existing `SearchHistoryService`/`searchHistoryApi` (Phase 17) as-is.
- Wiring the org-admin `BusinessDashboardService` to anything new — it already shows organization summary, subscriptions, usage, and service-status alerts from earlier phases; "spending" stays explicitly unavailable pending 08 Billing.
- Pagination, result ranking/scoring, or a result count limit beyond each source's own existing behavior.

## Dependencies
- Existing `ProductService#searchProducts`, `KnowledgeArticleService#searchPublished`, `SupportTicketService#listMyTickets`.
- `CurrentCustomerResolver#resolveOptional` (existing) — resolves "who is calling," if anyone, without erroring for a signed-out caller.
- No new table or migration.
