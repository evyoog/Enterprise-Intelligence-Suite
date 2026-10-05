# TC-KNW-043: Search results grouped by type with products and modules

| Field | Value |
|---|---|
| Test Case ID (required) | TC-KNW-043 |
| Requirement ID (required) | [REQ-KNW-005](../../../docs/02-requirements/FRD/knowledge-center/requirement.md) |
| Acceptance Criterion | [AC-4](../../../docs/02-requirements/FRD/knowledge-center/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-KNW-002](../knowledge-base/TESTPLAN-KNW-002.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
Seed data loaded (C74). Storage replaced by the in-memory store in automated tests.

## Steps
Search "purchase".

## Expected Result
Groups by type, matching products and modules, a support link with the query.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/knowledgebase/service/KnowledgeAnalyticsAndSearchTest.java` — `publicKnowledgeOfEveryTypeIsSearchableButRestrictedIsNotInGlobalSearch`
- `frontend/src/pages/knowledge/KnowledgeCenter.test.tsx` — `groups search results by type and offers a ticket with the query`

## Actual Result
The automated tests above passed on 2026-10-05.

## Status
Passed (automated)
