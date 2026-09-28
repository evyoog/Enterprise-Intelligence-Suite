# Acceptance criteria — Knowledge Base

Each criterion maps to at least one test case in `test-cases/functional/knowledge-base/`.

| ID | Criterion | Test cases |
|----|-----------|------------|
| AC-1 | **Given** an admin creates an article **Then** it starts as DRAFT with version 1, and is invisible to public search/read | TC-KNW-001 |
| AC-2 | **Given** a DRAFT article **When** an admin publishes it **Then** it appears in public search results and its detail is readable | TC-KNW-002 |
| AC-3 | **Given** a PUBLISHED article **When** an admin unpublishes it **Then** it disappears from public search and its detail returns 404 again | TC-KNW-003 |
| AC-4 | **Given** any article **When** an admin edits its title/body **Then** its version counter increments by 1 | TC-KNW-004 |
| AC-5 | **Given** a published article **When** the public search query matches its title or body, case-insensitively **Then** it is returned; a non-matching query excludes it | TC-KNW-005 |
| AC-6 | **Given** an admin deletes an article **Then** it no longer appears in the admin list | TC-KNW-006 |
