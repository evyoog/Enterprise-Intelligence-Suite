# TC-KNW-027: Downloads only for readers allowed to see content using the file

| Field | Value |
|---|---|
| Test Case ID (required) | TC-KNW-027 |
| Requirement ID (required) | [REQ-KNW-003](../../../docs/02-requirements/FRD/knowledge-media/requirement.md) |
| Acceptance Criterion | [AC-4](../../../docs/02-requirements/FRD/knowledge-media/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-KNW-002](../knowledge-base/TESTPLAN-KNW-002.md) |
| Priority | P0 |
| Type | Security |
| Automated | Yes |

## Preconditions
Seed data loaded (C74). Storage replaced by the in-memory store in automated tests.

## Steps
Attach a PDF to organization 701 content; download as 701, 702 and signed out, before and after publishing.

## Expected Result
Only 701 after publishing gets a 5-minute URL; others 404.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/knowledgebase/service/KnowledgeMediaAndVideoServiceTest.java` — `downloadIsOnlyForReadersWhoMaySeeContentUsingTheFile`

## Actual Result
The automated tests above passed on 2026-10-05.

## Status
Passed (automated)
