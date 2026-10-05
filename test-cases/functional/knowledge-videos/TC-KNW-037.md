# TC-KNW-037: Transcript and chapters on the player

| Field | Value |
|---|---|
| Test Case ID (required) | TC-KNW-037 |
| Requirement ID (required) | [REQ-KNW-004](../../../docs/02-requirements/FRD/knowledge-videos/requirement.md) |
| Acceptance Criterion | [AC-6](../../../docs/02-requirements/FRD/knowledge-videos/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-KNW-002](../knowledge-base/TESTPLAN-KNW-002.md) |
| Priority | P1 |
| Type | UI |
| Automated | Partly |

## Preconditions
Seed data loaded (C74). Storage replaced by the in-memory store in automated tests.

## Steps
Open a video with transcript and chapters; search the transcript; click a chapter.

## Expected Result
Transcript filters; the player jumps to the chapter.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/knowledgebase/service/KnowledgeMediaAndVideoServiceTest.java` — `chaptersAndYouTubeLinksAreParsedStrictly`

## Manual / UAT
- UAT-KNW-004 step 3

## Actual Result
The automated tests above passed on 2026-10-05.

## Status
Automated part passed; manual part pending UAT
