# TC-KNW-046: "No" feedback needs a reason and reaches analytics

| Field | Value |
|---|---|
| Test Case ID (required) | TC-KNW-046 |
| Requirement ID (required) | [REQ-KNW-005](../../../docs/02-requirements/FRD/knowledge-center/requirement.md) |
| Acceptance Criterion | [AC-7](../../../docs/02-requirements/FRD/knowledge-center/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-KNW-002](../knowledge-base/TESTPLAN-KNW-002.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
Seed data loaded (C74). Storage replaced by the in-memory store in automated tests.

## Steps
Click No, pick Outdated, send.

## Expected Result
Stored once per reader per version; lowest-rated list updates.

## Automated coverage
- `frontend/src/pages/knowledge/KnowledgeCenter.test.tsx` — `sends a No vote only with a reason`
- `backend/src/test/java/com/vyoog/eisplatform/modules/knowledgebase/service/KnowledgeAnalyticsAndSearchTest.java` — `feedbackIsOneVotePerReaderAndNoNeedsAReason`

## Actual Result
The automated tests above passed on 2026-10-05.

## Status
Passed (automated)
