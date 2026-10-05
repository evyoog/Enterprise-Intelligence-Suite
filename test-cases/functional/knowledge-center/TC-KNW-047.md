# TC-KNW-047: Create support ticket is prefilled from the page

| Field | Value |
|---|---|
| Test Case ID (required) | TC-KNW-047 |
| Requirement ID (required) | [REQ-KNW-005](../../../docs/02-requirements/FRD/knowledge-center/requirement.md) |
| Acceptance Criterion | [AC-8](../../../docs/02-requirements/FRD/knowledge-center/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-KNW-002](../knowledge-base/TESTPLAN-KNW-002.md) |
| Priority | P1 |
| Type | UI |
| Automated | Partly |

## Preconditions
Seed data loaded (C74). Storage replaced by the in-memory store in automated tests.

## Steps
On an error code page click Create support ticket; send it.

## Expected Result
The ticket form opens with subject and context; editable; TICKET_CREATED_FROM_KNOWLEDGE recorded.

## Automated coverage
- `frontend/src/pages/knowledge/KnowledgeCenter.test.tsx` — `groups search results by type and offers a ticket with the query`

## Manual / UAT
- UAT-KNW-004 step 5

## Actual Result
The automated tests above passed on 2026-10-05.

## Status
Automated part passed; manual part pending UAT
