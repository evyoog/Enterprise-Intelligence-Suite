# TC-KNW-051: Knowledge gaps are detected

| Field | Value |
|---|---|
| Test Case ID (required) | TC-KNW-051 |
| Requirement ID (required) | [REQ-KNW-006](../../../docs/02-requirements/FRD/knowledge-analytics/requirement.md) |
| Acceptance Criterion | [AC-2](../../../docs/02-requirements/FRD/knowledge-analytics/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-KNW-002](../knowledge-base/TESTPLAN-KNW-002.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
Seed data loaded (C74). Storage replaced by the in-memory store in automated tests.

## Steps
Search a query with no results 5 times in the Knowledge Center.

## Expected Result
"Knowledge gap detected — … — 5 searches — 0 results" with Create content.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/knowledgebase/service/KnowledgeAnalyticsAndSearchTest.java` — `viewsAreCountedOncePerReaderPerHalfHourAndGapsAreDetected`

## Actual Result
The automated tests above passed on 2026-10-05.

## Status
Passed (automated)
