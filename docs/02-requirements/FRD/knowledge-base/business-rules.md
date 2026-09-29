# Business rules — Knowledge Base

| ID | Rule | Enforced in | Source |
|----|------|-------------|--------|
| BR-KNW-001 | A newly created article always starts as DRAFT with version 1, regardless of what the request contains. | backend | REQ-KNW-001.1 |
| BR-KNW-002 | Editing an article increments its version counter by exactly 1 on every call, whether or not the title/body actually changed. | backend | REQ-KNW-001.2 |
| BR-KNW-003 | Publish/unpublish is a plain status toggle (DRAFT ↔ PUBLISHED) — either direction is always allowed, no other precondition. | backend | REQ-KNW-001.3 |
| BR-KNW-004 | Public search (`searchPublished`) only ever considers articles with `status = PUBLISHED`; a blank/absent query returns all of them, newest-updated first. | backend | REQ-KNW-001.5 |
| BR-KNW-005 | Public article-by-id read refuses (404) an article that is not currently PUBLISHED, identically to a nonexistent id — never confirming a DRAFT article's existence. | backend | REQ-KNW-001.6 |
| BR-KNW-006 | Every admin mutation (create/edit/publish/unpublish/delete) is recorded in the audit log (`KNOWLEDGE_ARTICLE_CREATED`/`_EDITED`/`_PUBLISHED`/`_UNPUBLISHED`/`_DELETED`). | backend | REQ-KNW-001.1–.4 |

Rules shared with other features belong in `docs/03-business-rules/` and are referenced here by ID.
