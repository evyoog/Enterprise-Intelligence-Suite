# TC-PRT-022: Partial words, typos, "Did you mean" and exact IDs

| Field | Value |
|---|---|
| Test Case ID (required) | TC-PRT-022 |
| Requirement ID (required) | [REQ-PRT-002](../../../docs/02-requirements/FRD/global-search/requirement.md) |
| Acceptance Criterion | [AC-7](../../../docs/02-requirements/FRD/global-search/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-PRT-002](TESTPLAN-PRT-002.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
Products and an article with distinctive words.

## Steps
1. Search the first letters of a product name.
2. Search a misspelling of a word that only appears in an article body.
3. Search a misspelt product name.
4. Search "#<product id>".

## Expected Result
Labels: Partial word; Close spelling with "Did you mean <correct words>"; the misspelt name finds the product; Exact ID result first with reference "#id".

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/search/service/PostgresSearchIntegrationTest.java` — `findsPartialWords`, `correctsTyposAndSuggestsTheCorrection`, `typoInATitleMatchesWithoutCorrection`, `exactIdComesFirst`, `highlightsMatchingWords`

## Actual Result
The automated tests above passed on 2026-10-05 (PostgreSQL 16 with pg_trgm and pgvector 0.6.0; embeddings from the stub test model).

## Status
Passed (automated run 2026-10-05)

## Linked Defect (if failed)
None.
