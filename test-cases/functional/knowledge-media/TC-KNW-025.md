# TC-KNW-025: Disallowed types and oversized files are refused

| Field | Value |
|---|---|
| Test Case ID (required) | TC-KNW-025 |
| Requirement ID (required) | [REQ-KNW-003](../../../docs/02-requirements/FRD/knowledge-media/requirement.md) |
| Acceptance Criterion | [AC-2](../../../docs/02-requirements/FRD/knowledge-media/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-KNW-002](../knowledge-base/TESTPLAN-KNW-002.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
Seed data loaded (C74). Storage replaced by the in-memory store in automated tests.

## Steps
Request upload URLs for .exe, .svg and a video over the limit; a module of another product.

## Expected Result
INVALID_FILE_TYPE / FILE_TOO_LARGE / INVALID_CONTENT with friendly messages.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/knowledgebase/service/KnowledgeMediaAndVideoServiceTest.java` — `uploadUrlUsesAGeneratedKeyAndShortExpiryAndChecksTypeAndSize`

## Actual Result
The automated tests above passed on 2026-10-05.

## Status
Passed (automated)
