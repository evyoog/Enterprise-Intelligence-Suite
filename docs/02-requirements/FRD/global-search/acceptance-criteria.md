# Acceptance criteria — Global Search

Each criterion maps to at least one test case in `test-cases/functional/global-search/`.

| ID | Criterion | Test cases |
|----|-----------|------------|
| AC-1 | **Given** an ACTIVE product matching a query **When** a global search runs **Then** it appears in the products section | TC-PRT-001 |
| AC-2 | **Given** a PUBLISHED knowledge article matching a query **When** a global search runs **Then** it appears in the knowledge base section | TC-PRT-002 |
| AC-3 | **Given** a customer's own ticket matching a query **When** that customer runs a global search **Then** it appears in the tickets section, but is absent when the same search runs with no linked customer | TC-PRT-003 |
| AC-4 | **Given** results across multiple types **When** `type` is set to one of them **Then** only that type's results are returned | TC-PRT-004 |
| AC-5 | **Given** members of two organizations with similar tickets **When** each searches (keyword or hybrid, by words or by ticket ID, or via suggestions) **Then** each sees only their own tickets, and a signed-out caller sees none | TC-PRT-020 |
| AC-6 | **Given** content in English and Spanish **When** a query uses another word form, no accents, or a quoted phrase **Then** the matching record is found, a phrase match ranks first, and a quoted phrase finds only the phrase | TC-PRT-021 |
| AC-7 | **Given** a misspelt, partial or ID query **When** it runs **Then** the record is found with the label Close spelling, Partial word or Exact ID, and a correction is offered as "Did you mean" | TC-PRT-022 |
| AC-8 | **Given** a product, article or ticket changes, is retired, unpublished or deleted **When** the change commits **Then** search reflects it at once; a rolled-back change is not indexed; Rebuild restores a missing record | TC-PRT-023 |
| AC-9 | **Given** a synonym group **When** one of its terms is searched **Then** records with the other terms are found | TC-PRT-024 |
| AC-10 | **Given** the search UI **When** the user types in the top bar, presses Ctrl+K, uses arrow keys, or opens the results page **Then** suggestions, recent searches, match labels, highlights, filters, "Did you mean", zero-result help and Clear history behave as specified | TC-PRT-025 |
| AC-11 | **Given** a platform admin with `MANAGE_SEARCH` **When** they open Search administration **Then** they see the index state, can rebuild it, manage synonyms and see insights; others get 403 | TC-PRT-026 |
| AC-12 | **Given** the quality test set **When** keyword search runs **Then** hit@5 is at least the pre-C70 search's and keyword p95 is under 500 ms | TC-PRT-027 |
