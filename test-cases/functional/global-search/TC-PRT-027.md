# TC-PRT-027: Quality test set and speed

| Field | Value |
|---|---|
| Test Case ID (required) | TC-PRT-027 |
| Requirement ID (required) | [REQ-PRT-002](../../../docs/02-requirements/FRD/global-search/requirement.md), [REQ-PRT-003](../../../docs/02-requirements/FRD/semantic-search/requirement.md) |
| Acceptance Criterion | [AC-12](../../../docs/02-requirements/FRD/global-search/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-PRT-002](TESTPLAN-PRT-002.md) |
| Priority | P0 |
| Type | Performance |
| Automated | Yes |

## Preconditions
Empty PostgreSQL schema; EIS_SEARCH_REPORT=1.

## Steps
1. Load the 36-record test set and run the 40 English and Spanish queries with the pre-C70 search, keyword search and hybrid search.
2. Tune the thresholds and chunk size.
3. Add 13,000 synthetic records and time every query 10 times.

## Expected Result
Keyword hit@5 ≥ the pre-C70 search's; keyword p95 < 500 ms; hybrid p95 < 800 ms. Report written to the FRD.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/search/service/SearchQualityReportTest.java` — `writeReport` (run on request)
- `docs/02-requirements/FRD/semantic-search/quality-report.md`

## Actual Result
The automated tests above passed on 2026-10-05 (PostgreSQL 16 with pg_trgm and pgvector 0.6.0; embeddings from the stub test model).

## Status
Passed (automated run 2026-10-05)

## Linked Defect (if failed)
None.
