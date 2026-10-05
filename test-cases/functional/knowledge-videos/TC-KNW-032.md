# TC-KNW-032: YouTube details without an API key come from oEmbed

| Field | Value |
|---|---|
| Test Case ID (required) | TC-KNW-032 |
| Requirement ID (required) | [REQ-KNW-004](../../../docs/02-requirements/FRD/knowledge-videos/requirement.md) |
| Acceptance Criterion | [AC-1](../../../docs/02-requirements/FRD/knowledge-videos/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-KNW-002](../knowledge-base/TESTPLAN-KNW-002.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
Seed data loaded (C74). Storage replaced by the in-memory store in automated tests.

## Steps
Paste a YouTube link and click Fetch video details with no key configured.

## Expected Result
Title, channel and thumbnail filled; duration and description typed by hand.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/knowledgebase/service/YouTubeOembedTest.java` — `oembedFillsTitleChannelAndThumbnail`
- `frontend/src/pages/knowledge-admin/KnowledgeManagement.test.tsx` — `fetches YouTube details with the oEmbed fallback and says storage is off for uploads`

## Actual Result
The automated tests above passed on 2026-10-05.

## Status
Passed (automated)
