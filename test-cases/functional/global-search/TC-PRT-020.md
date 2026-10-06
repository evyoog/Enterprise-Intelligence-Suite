# TC-PRT-020: One organization never sees another organization's records

| Field | Value |
|---|---|
| Test Case ID (required) | TC-PRT-020 |
| Requirement ID (required) | [REQ-PRT-002](../../../docs/02-requirements/FRD/global-search/requirement.md) |
| Acceptance Criterion | [AC-5](../../../docs/02-requirements/FRD/global-search/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-PRT-002](TESTPLAN-PRT-002.md) |
| Priority | P0 |
| Type | Security |
| Automated | Yes |

## Preconditions
Two organizations, one member each; each member has a ticket with the same words. PostgreSQL search index installed.

## Steps
1. Search the shared words as member A, in keyword and hybrid mode.
2. Repeat as member B and signed out.
3. As member A, search member B's ticket ID ("#id", type TICKET).
4. As member A, ask for suggestions.

## Expected Result
Member A sees only their own ticket; member B only theirs; signed out sees no tickets; the ID search and the suggestions never show the other organization's ticket.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/search/service/PostgresSearchIntegrationTest.java` — `oneOrganizationNeverSeesAnothersRecords`, `didYouMeanNeverUsesPrivateTicketWords`

## Actual Result
The automated tests above passed on 2026-10-05 (PostgreSQL 16 with pg_trgm and pgvector 0.6.0; embeddings from the stub test model).

## Status
Passed (automated run 2026-10-05)

## Linked Defect (if failed)
None.
