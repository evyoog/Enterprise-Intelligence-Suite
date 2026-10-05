# TC-PRT-028: A record found by meaning is labelled "Similar meaning"

| Field | Value |
|---|---|
| Test Case ID (required) | TC-PRT-028 |
| Requirement ID (required) | [REQ-PRT-003](../../../docs/02-requirements/FRD/semantic-search/requirement.md) |
| Acceptance Criterion | [AC-1](../../../docs/02-requirements/FRD/semantic-search/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-PRT-004](TESTPLAN-PRT-004.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
Stub embedding service; a published article.

## Steps
1. Index and embed.
2. Run a hybrid search with some of the article's words plus a word it does not contain.

## Expected Result
`semanticStatus` USED; the article is in the results with match type SEMANTIC.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/search/service/PostgresSearchIntegrationTest.java` — `hybridSearchAddsSemanticMatches`
- `backend/src/test/java/com/vyoog/eisplatform/modules/search/service/SearchTextUtilitiesTest.java` — `hybridMergeKeepsExactIdFirstAndRewardsBothLists`, `chunksWithOverlapAndTitle`

## Actual Result
The automated tests above passed on 2026-10-05 (PostgreSQL 16 with pg_trgm and pgvector 0.6.0; embeddings from the stub test model).

## Status
Passed (automated run 2026-10-05)

## Linked Defect (if failed)
None.
