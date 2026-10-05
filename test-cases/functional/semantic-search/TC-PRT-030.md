# TC-PRT-030: Private records are never embedded

| Field | Value |
|---|---|
| Test Case ID (required) | TC-PRT-030 |
| Requirement ID (required) | [REQ-PRT-003](../../../docs/02-requirements/FRD/semantic-search/requirement.md) |
| Acceptance Criterion | [AC-3](../../../docs/02-requirements/FRD/semantic-search/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-PRT-004](TESTPLAN-PRT-004.md) |
| Priority | P0 |
| Type | Security |
| Automated | Yes |

## Preconditions
A support ticket.

## Steps
1. Index and embed everything.
2. Count passages of non-public documents.
3. Search with type TICKET in hybrid mode.

## Expected Result
Zero passages for non-public documents; `semanticStatus` NOT_APPLICABLE for ticket searches.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/search/service/PostgresSearchIntegrationTest.java` — `privateRecordsAreNeverEmbedded`, `keywordModeAndTicketTypeSkipSemantic`

## Actual Result
The automated tests above passed on 2026-10-05 (PostgreSQL 16 with pg_trgm and pgvector 0.6.0; embeddings from the stub test model).

## Status
Passed (automated run 2026-10-05)

## Linked Defect (if failed)
None.
