# Acceptance criteria — Global Search

Each criterion maps to at least one test case in `test-cases/functional/global-search/`.

| ID | Criterion | Test cases |
|----|-----------|------------|
| AC-1 | **Given** an ACTIVE product matching a query **When** a global search runs **Then** it appears in the products section | TC-PRT-001 |
| AC-2 | **Given** a PUBLISHED knowledge article matching a query **When** a global search runs **Then** it appears in the knowledge base section | TC-PRT-002 |
| AC-3 | **Given** a customer's own ticket matching a query **When** that customer runs a global search **Then** it appears in the tickets section, but is absent when the same search runs with no linked customer | TC-PRT-003 |
| AC-4 | **Given** results across multiple types **When** `type` is set to one of them **Then** only that type's results are returned | TC-PRT-004 |
