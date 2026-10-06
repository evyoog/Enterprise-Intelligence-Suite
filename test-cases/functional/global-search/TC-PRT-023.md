# TC-PRT-023: Automatic re-indexing and Rebuild index

| Field | Value |
|---|---|
| Test Case ID (required) | TC-PRT-023 |
| Requirement ID (required) | [REQ-PRT-002](../../../docs/02-requirements/FRD/global-search/requirement.md) |
| Acceptance Criterion | [AC-8](../../../docs/02-requirements/FRD/global-search/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-PRT-002](TESTPLAN-PRT-002.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
Search index installed.

## Steps
1. Create a product, search it; rename it; search the old and new names; retire it.
2. Publish, unpublish, publish and delete an article, searching after each step.
3. Save a product inside a transaction that rolls back.
4. Delete an article's index row by hand, then rebuild.

## Expected Result
Search always reflects the committed state; the rolled-back product is never indexed; the rebuild restores the missing row and finishes DONE (also without the PostgreSQL index).

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/search/service/PostgresSearchIntegrationTest.java` — `reindexesWhenContentChanges`, `unpublishedAndDeletedArticlesLeaveTheIndex`, `rebuildRestoresTheIndex`
- `backend/src/test/java/com/vyoog/eisplatform/modules/search/service/SearchIndexListenerTest.java` — all five tests

## Actual Result
The automated tests above passed on 2026-10-05 (PostgreSQL 16 with pg_trgm and pgvector 0.6.0; embeddings from the stub test model).

## Status
Passed (automated run 2026-10-05)

## Linked Defect (if failed)
None.
