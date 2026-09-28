# Business rules — Global Search

| ID | Rule | Enforced in | Source |
|----|------|-------------|--------|
| BR-PRT-001 | Product results come from `ProductService#searchProducts(query, null, null, null, null, false)` — ACTIVE products only, the same text match the public catalog search already uses. | backend | REQ-PRT-002.1 |
| BR-PRT-002 | Knowledge article results come from `KnowledgeArticleService#searchPublished(query)` — PUBLISHED articles only. | backend | REQ-PRT-002.1 |
| BR-PRT-003 | Ticket results are the caller's own (`SupportTicketService#listMyTickets`), filtered in-memory by a case-insensitive substring match on subject or description; they are only included when the caller's JWT resolves to a linked Customer row (`CurrentCustomerResolver#resolveOptional`), never for a signed-out caller or one with no linked account. | backend | REQ-PRT-002.1, .3 |
| BR-PRT-004 | `type` (case-insensitive) restricts the response to exactly one source; any other/absent value returns all three. | backend | REQ-PRT-002.2 |
| BR-PRT-005 | A blank or absent query returns every item each source would return unfiltered (all ACTIVE products / all PUBLISHED articles / all the caller's own tickets) — "no query" is not itself an error. | backend | REQ-PRT-002.1 |

Rules shared with other features belong in `docs/03-business-rules/` and are referenced here by ID.
