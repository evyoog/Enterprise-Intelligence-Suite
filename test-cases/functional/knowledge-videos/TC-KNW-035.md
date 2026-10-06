# TC-KNW-035: Wrong formats and oversized videos are refused

| Field | Value |
|---|---|
| Test Case ID (required) | TC-KNW-035 |
| Requirement ID (required) | [REQ-KNW-004](../../../docs/02-requirements/FRD/knowledge-videos/requirement.md) |
| Acceptance Criterion | [AC-4](../../../docs/02-requirements/FRD/knowledge-videos/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-KNW-002](../knowledge-base/TESTPLAN-KNW-002.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
Seed data loaded (C74). Storage replaced by the in-memory store in automated tests.

## Steps
Choose an AVI, then an MP4 over the limit.

## Expected Result
Friendly error, nothing uploaded.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/knowledgebase/service/KnowledgeMediaAndVideoServiceTest.java` — `uploadUrlUsesAGeneratedKeyAndShortExpiryAndChecksTypeAndSize`

## Actual Result
The automated tests above passed on 2026-10-05.

## Status
Passed (automated)
