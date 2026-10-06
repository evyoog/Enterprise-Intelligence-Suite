# TC-KNW-050: Views, feedback and video events reach analytics

| Field | Value |
|---|---|
| Test Case ID (required) | TC-KNW-050 |
| Requirement ID (required) | [REQ-KNW-006](../../../docs/02-requirements/FRD/knowledge-analytics/requirement.md) |
| Acceptance Criterion | [AC-1](../../../docs/02-requirements/FRD/knowledge-analytics/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-KNW-002](../knowledge-base/TESTPLAN-KNW-002.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
Seed data loaded (C74). Storage replaced by the in-memory store in automated tests.

## Steps
Open an item twice as one reader and once as another; vote; play a video.

## Expected Result
Views counted once per reader per 30 minutes; helpful % and plays shown.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/knowledgebase/service/KnowledgeAnalyticsAndSearchTest.java` — `viewsAreCountedOncePerReaderPerHalfHourAndGapsAreDetected`
- `backend/src/test/java/com/vyoog/eisplatform/modules/knowledgebase/service/KnowledgeAnalyticsAndSearchTest.java` — `feedbackIsOneVotePerReaderAndNoNeedsAReason`

## Actual Result
The automated tests above passed on 2026-10-05.

## Status
Passed (automated)
