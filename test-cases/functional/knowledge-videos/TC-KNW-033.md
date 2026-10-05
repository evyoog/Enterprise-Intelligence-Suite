# TC-KNW-033: With an API key the Data API fills every field

| Field | Value |
|---|---|
| Test Case ID (required) | TC-KNW-033 |
| Requirement ID (required) | [REQ-KNW-004](../../../docs/02-requirements/FRD/knowledge-videos/requirement.md) |
| Acceptance Criterion | [AC-2](../../../docs/02-requirements/FRD/knowledge-videos/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-KNW-002](../knowledge-base/TESTPLAN-KNW-002.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
Seed data loaded (C74). Storage replaced by the in-memory store in automated tests.

## Steps
Configure EIS_YOUTUBE_API_KEY and fetch details.

## Expected Result
Id, thumbnail, title, description, duration and channel filled.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/knowledgebase/service/YouTubeOembedTest.java` — `withAnApiKeyTheDataApiFillsDurationAndDescription`

## Actual Result
The automated tests above passed on 2026-10-05.

## Status
Passed (automated)
