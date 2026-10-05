# Business rules — Global Search

| ID | Rule | Enforced in | Source |
|----|------|-------------|--------|
| BR-PRT-001 | Product results are ACTIVE products only. Basic engine: `ProductService#searchProducts(query, null, null, null, null, false)`; index: `SearchDocumentBuilder#fromProduct`. | backend | REQ-PRT-002.1 |
| BR-PRT-002 | Knowledge article results are PUBLISHED articles only. | backend | REQ-PRT-002.1 |
| BR-PRT-003 | Ticket results are the caller's own tickets only, and only when the caller's JWT resolves to a linked Customer row (`CurrentCustomerResolver#resolveOptional`); never for a signed-out caller or one with no linked account. | backend | REQ-PRT-002.1, .3 |
| BR-PRT-004 | `type` (case-insensitive: PRODUCT, KNOWLEDGE, TICKET) restricts the response to one source; absent returns all three; an unknown value returns no results. | backend | REQ-PRT-002.2 |
| BR-PRT-005 | A blank or absent query returns every item each source would return unfiltered — "no query" is not an error. | backend | REQ-PRT-002.1 |
| BR-SRCH-001 | **Visibility before ranking.** Every index query keeps only rows the caller may see — `visibility = PUBLIC` or `owner_customer_id` = the caller — and the requested type, before any row is scored. A record of another customer or organization is never scored, suggested or counted. | backend (`SearchSqlRepository`) | REQ-PRT-002.5, C70 |
| BR-SRCH-002 | **Automatic re-indexing.** After a transaction that inserts, updates or deletes a product (or its platforms), a knowledge article or a support ticket commits, that record is re-indexed. A rolled-back change is never indexed. A failure to index never fails the user's change; the next rebuild repairs it. | backend (`SearchChangeListener`) | REQ-PRT-002.5 |
| BR-SRCH-003 | **Match tiers.** 1 Exact ID ("#42" or "42"), 2 Exact phrase (all words in order), 3 Keyword (all words; English or Spanish word forms; synonyms), 4 Partial word (every word as a prefix), 5 Typo (every word close to a title word, or the corrected query matches). Results are ordered by tier, then relevance. | backend | REQ-PRT-002.6–.9 |
| BR-SRCH-004 | **Folding.** Indexed text and queries are lower-cased, accents removed and punctuation turned into spaces ("Facturación" = "facturacion"; "sign-on" = "sign on"). | backend (`TextFolding`) | REQ-PRT-002.7 |
| BR-SRCH-005 | **Quoted phrases.** Words in double quotes must match as a phrase; quotes turn off the partial-word and typo tiers. | backend | REQ-PRT-002.6 |
| BR-SRCH-006 | **Did you mean.** Only when tiers 1–4 find nothing: each word of 3+ letters that is not in public content is replaced by the most similar public word (similarity ≥ correction threshold); if the corrected query finds results, they are shown as Typo matches with "Did you mean". Words of private records (tickets) are never used for corrections. | backend | REQ-PRT-002.11 |
| BR-SRCH-007 | **Highlighting.** A word is highlighted when its folded form equals a query word or one starts with the other's stem (the word minus up to two letters, at least 3). Synonyms and corrections are highlighted too. Highlights are character ranges; the text is never treated as HTML. | backend (`Highlighter`), frontend (`HighlightedText`) | REQ-PRT-002.10 |
| BR-SRCH-008 | **Synonyms.** A synonym group has 2 to 10 different terms (folded). Searching any term also matches the others; a multi-word term matches as a phrase. | backend (`SynonymService`) | REQ-PRT-002.17 |
| BR-SRCH-009 | **Insights.** A search is logged (query text, result count, mode, whether semantic search was used, time taken) only when the request has `track=true`. No user identity is stored. | backend | REQ-PRT-002.17 |
| BR-SRCH-010 | **Basic engine.** When the index tables are not installed (or the database is not PostgreSQL), and for a blank query, search runs the matching that existed before C70 (BR-PRT-001–005) with highlighting added. | backend | REQ-PRT-002.5 |
| BR-SRCH-011 | **Rebuild.** One rebuild at a time (409 otherwise). It re-reads every source record, removes index rows whose record no longer exists or is not searchable, and refreshes the word list. Search keeps working meanwhile. | backend | REQ-PRT-002.16 |

Thresholds: typo 0.4, correction 0.45 (pg_trgm similarity), chosen with the quality test set — [quality report](../semantic-search/quality-report.md).
Rules shared with other features belong in `docs/03-business-rules/` and are referenced here by ID.
