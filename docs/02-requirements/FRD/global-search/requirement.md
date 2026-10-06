# REQ-PRT-002 — Global Search

**Status:** Approved
**BRD:** Not specified
**Owner:** Product owner
**Approved by / on:** Product owner / 2026-09-28 ([C41](../../../01-business/roadmap/open-decisions.md#c41)); keyword-search improvements REQ-PRT-002.5–.17 approved 2026-10-05 ([C70](../../../01-business/roadmap/open-decisions.md#c70))

| Field | Value |
|---|---|
| Sprint | [2027.1.3](../../../01-business/roadmap/sprints/SPRINT-2027.1.3.md) |
| Requirement ID | REQ-PRT-002 |
| Application | [01 Enterprise Intelligence Suite](../../../01-business/roadmap/applications/01-enterprise-intelligence-suite.md) |
| Application code | `APP-PRT` |
| Priority | P0 ([C4](../../../01-business/roadmap/open-decisions.md#c4), [C5](../../../01-business/roadmap/open-decisions.md#c5)) |
| AI required | No ([C12](../../../01-business/roadmap/open-decisions.md#c12)) — semantic search is its own requirement, [REQ-PRT-003](../semantic-search/requirement.md) |
| Built | Original scope 2026-09-28; C70 improvements built early on 2026-10-05 (sprint dates unchanged) |

## Source functions
| Function ID | Function | Application page |
|---|---|---|
| 01.03.01 | Keyword search; Filter results; Sort results; View search history | [01 Enterprise Intelligence Suite](../../../01-business/roadmap/applications/01-enterprise-intelligence-suite.md#0103-global-search) |

01.03.01.02 Semantic search is covered by [REQ-PRT-003](../semantic-search/requirement.md) ([C70](../../../01-business/roadmap/open-decisions.md#c70)), not by this requirement. 01.02 Customer Dashboard's own "on live data" functions are already satisfied by the existing `BusinessDashboardService` (built in earlier phases) and are likewise not covered here.

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
| REQ-PRT-002.4 | ~~Each result source keeps its own existing sort order~~ — replaced by REQ-PRT-002.9 (C70). The original order still applies to a blank query and to the basic engine (BR-SRCH-010). | Should |
| REQ-PRT-002.5 | **Search index (C70).** Every searchable record is kept in a search index (`search_document`): ACTIVE products, PUBLISHED articles and support tickets, with the same visibility as before. A record is re-indexed automatically after every committed change and removed when it is deleted or stops being searchable. | Must |
| REQ-PRT-002.6 | **Exact phrase.** Words in double quotes match only as that phrase. Without quotes, results that contain the words as a phrase rank above results that contain them apart. | Must |
| REQ-PRT-002.7 | **Word forms, English and Spanish.** A word matches its other forms in both languages (invoice / invoices, pagar / pagos). Case and accents are ignored (facturacion finds Facturación). | Must |
| REQ-PRT-002.8 | **Partial words, typos and exact IDs.** A word matches longer words that start with it (varth → Varthan.ai); a close misspelling matches; "#42" or "42" finds the record with that ID first. | Must |
| REQ-PRT-002.9 | **Ranking.** Results come in one list, best first: exact ID, exact phrase, all words, partial words, typos; within each, by relevance. The grouped lists stay in the response. | Must |
| REQ-PRT-002.10 | **Highlighting.** The words that matched are highlighted in each result's title and snippet; the snippet is taken around the first match. | Must |
| REQ-PRT-002.11 | **Did you mean.** When nothing matches and a misspelt word has a close word in public content, the search runs with the corrected words and offers "Did you mean …". | Must |
| REQ-PRT-002.12 | **Filters.** All, Products, Knowledge base, Your tickets (signed in). | Must |
| REQ-PRT-002.13 | **Search history.** Signed-in users see their recent searches and can clear them. | Must |
| REQ-PRT-002.14 | **Zero-result help.** When nothing matches, the page explains what to try and links to the catalog, the knowledge base and (signed in) support. | Must |
| REQ-PRT-002.15 | **Search UI.** Suggestions while typing in the top bar, Ctrl+K / ⌘K to focus it, and a results page with a match label on every result. | Must |
| REQ-PRT-002.16 | **Rebuild index.** A platform admin can rebuild the whole index and see its state. | Must |
| REQ-PRT-002.17 | **Insights and synonyms.** A platform admin sees what people search for, which searches find nothing and how fast search answers, and manages synonym groups that keyword search treats as equal. | Must |

## Out of scope
- Semantic search — its own requirement, [REQ-PRT-003](../semantic-search/requirement.md).
- AI-written answers above the results (C70: later).
- New search-history storage — the existing `SearchHistoryService`/`searchHistoryApi` (Phase 17) is reused; C70 only adds it to the search page and the top bar.
- Filters other than type (for example date, category, platform): **Not specified**.
- Searching records other than products, published articles and the caller's own tickets: **Not specified**.
- Wiring the org-admin `BusinessDashboardService` to anything new — it already shows organization summary, subscriptions, usage, and service-status alerts from earlier phases; "spending" stays explicitly unavailable pending 08 Billing.
- Pagination. Results are limited to 50 (`app.search.result-limit`, at most 100 per request); paging beyond that is **Not specified**.

## Dependencies
- Existing `ProductService#searchProducts`, `KnowledgeArticleService#searchPublished`, `SupportTicketService#listMyTickets`.
- `CurrentCustomerResolver#resolveOptional` (existing) — resolves "who is calling," if anyone, without erroring for a signed-out caller.
- C70: tables `search_document`, `search_term`, `search_synonym`, `search_query_log`, `search_index_run` (PostgreSQL extension `pg_trgm`) — `database/migrations/V020__search_index.sql`. Without them the backend uses the basic engine (BR-SRCH-010).

## Engineering defaults (C70) — Not specified in the prompt, decided while building

| Topic | Default |
|---|---|
| Detailed specification | `PROMPT-keyword-and-semantic-search.md` was not in the repository; the product owner chose to build from the C70 prompt alone |
| Exact ID format | "#" followed by the record number, or the number alone |
| Typo match | Every query word must be close to a title word (word similarity ≥ typo threshold), or the query matches after correction |
| "Did you mean" | Only when nothing matches; corrections come only from words of public content |
| Suggestions | Up to 8 titles; recent searches: the last 5 |
| Tracked searches | A search counts in history and insights when the user presses Enter, picks a suggestion, follows "Did you mean", or arrives from the top bar — not while typing |
| Insights retention | `search_query_log` rows are kept indefinitely: **Not specified** |
| Permission | New platform permission `MANAGE_SEARCH` (seeded for ADMIN) for rebuild, synonyms and insights |
