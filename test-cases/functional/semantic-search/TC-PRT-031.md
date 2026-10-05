# TC-PRT-031: ai-service embedding endpoint

| Field | Value |
|---|---|
| Test Case ID (required) | TC-PRT-031 |
| Requirement ID (required) | [REQ-PRT-003](../../../docs/02-requirements/FRD/semantic-search/requirement.md) |
| Acceptance Criterion | [AC-4](../../../docs/02-requirements/FRD/semantic-search/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-PRT-004](TESTPLAN-PRT-004.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
ai-service test client.

## Steps
1. Call /embed/info and /embed with no provider.
2. With EMBEDDING_PROVIDER=stub, embed two texts; embed the same text with and without accents; send an empty list; use an unknown provider.

## Expected Result
503 when not configured; with the stub, two normalised 384-dimension vectors, identical for accented and plain text; 422 for an empty list; unknown provider reported not enabled.

## Automated coverage
- `ai-service/tests/test_embed.py` — 5 tests

## Actual Result
The automated tests above passed on 2026-10-05 (PostgreSQL 16 with pg_trgm and pgvector 0.6.0; embeddings from the stub test model).

## Status
Passed (automated run 2026-10-05)

## Linked Defect (if failed)
None.
