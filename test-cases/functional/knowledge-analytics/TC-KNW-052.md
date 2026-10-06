# TC-KNW-052: Analytics show no reader identities

| Field | Value |
|---|---|
| Test Case ID (required) | TC-KNW-052 |
| Requirement ID (required) | [REQ-KNW-006](../../../docs/02-requirements/FRD/knowledge-analytics/requirement.md) |
| Acceptance Criterion | [AC-3](../../../docs/02-requirements/FRD/knowledge-analytics/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-KNW-002](../knowledge-base/TESTPLAN-KNW-002.md) |
| Priority | P0 |
| Type | Security |
| Automated | Partly |

## Preconditions
Seed data loaded (C74). Storage replaced by the in-memory store in automated tests.

## Steps
Open Analytics and the dashboard.

## Expected Result
Only counts, titles and comments; no names, emails or ids of readers.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/knowledgebase/service/KnowledgeAnalyticsAndSearchTest.java` — `feedbackIsOneVotePerReaderAndNoNeedsAReason`

## Manual / UAT
- response shapes KnowledgeAnalyticsDto hold no reader fields

## Actual Result
The automated tests above passed on 2026-10-05.

## Status
Automated part passed; manual part pending UAT
