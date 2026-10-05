# TC-PRT-029: Keyword fallback when the model is down or slow

| Field | Value |
|---|---|
| Test Case ID (required) | TC-PRT-029 |
| Requirement ID (required) | [REQ-PRT-003](../../../docs/02-requirements/FRD/semantic-search/requirement.md) |
| Acceptance Criterion | [AC-2](../../../docs/02-requirements/FRD/semantic-search/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-PRT-004](TESTPLAN-PRT-004.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
Stub embedding service.

## Steps
1. Make the service answer 503, then search in hybrid mode.
2. Make it answer after 1.5 s, then search again.

## Expected Result
Both searches return the keyword results with `semanticStatus` UNAVAILABLE; the page shows the info message.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/search/service/PostgresSearchIntegrationTest.java` — `fallsBackToKeywordWhenTheModelIsDown`, `keywordModeAndTicketTypeSkipSemantic`
- `frontend/src/pages/GlobalSearchPage.test.tsx` — `tells the user when meaning search is unavailable`

## Actual Result
The automated tests above passed on 2026-10-05 (PostgreSQL 16 with pg_trgm and pgvector 0.6.0; embeddings from the stub test model).

## Status
Passed (automated run 2026-10-05)

## Linked Defect (if failed)
None.
