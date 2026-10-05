# TC-KNW-038: A phrase spoken only in a transcript finds the video

| Field | Value |
|---|---|
| Test Case ID (required) | TC-KNW-038 |
| Requirement ID (required) | [REQ-KNW-004](../../../docs/02-requirements/FRD/knowledge-videos/requirement.md) |
| Acceptance Criterion | [AC-7](../../../docs/02-requirements/FRD/knowledge-videos/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-KNW-002](../knowledge-base/TESTPLAN-KNW-002.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
Seed data loaded (C74). Storage replaced by the in-memory store in automated tests.

## Steps
Search for a word that appears only in a video transcript.

## Expected Result
The video is returned.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/knowledgebase/service/PostgresKnowledgeSearchTest.java` — `transcriptsAndAllPublicTypesAreSearchableAndRestrictedContentIsNotIndexed`

## Actual Result
The automated tests above passed on 2026-10-05.

## Status
Passed (automated)
