# TC-PRT-024: Synonyms

| Field | Value |
|---|---|
| Test Case ID (required) | TC-PRT-024 |
| Requirement ID (required) | [REQ-PRT-002](../../../docs/02-requirements/FRD/global-search/requirement.md) |
| Acceptance Criterion | [AC-9](../../../docs/02-requirements/FRD/global-search/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-PRT-002](TESTPLAN-PRT-002.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An article containing "receipt"; no article contains the test synonym.

## Steps
1. Search the synonym word: no result.
2. Add the group receipt + synonym.
3. Search the synonym word again.
4. Try to add a group with one term, and with the same term twice.

## Expected Result
After adding the group, the article is found; invalid groups are refused with 400.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/search/service/PostgresSearchIntegrationTest.java` — `synonymsFindEachOther`
- `backend/src/test/java/com/vyoog/eisplatform/config/SearchAuthorizationTest.java` — `synonymGroupsAreValidated`

## Actual Result
The automated tests above passed on 2026-10-05 (PostgreSQL 16 with pg_trgm and pgvector 0.6.0; embeddings from the stub test model).

## Status
Passed (automated run 2026-10-05)

## Linked Defect (if failed)
None.
