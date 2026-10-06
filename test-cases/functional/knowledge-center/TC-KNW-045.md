# TC-KNW-045: Article blocks render in order with glossary links and secure downloads

| Field | Value |
|---|---|
| Test Case ID (required) | TC-KNW-045 |
| Requirement ID (required) | [REQ-KNW-005](../../../docs/02-requirements/FRD/knowledge-center/requirement.md) |
| Acceptance Criterion | [AC-6](../../../docs/02-requirements/FRD/knowledge-center/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-KNW-002](../knowledge-base/TESTPLAN-KNW-002.md) |
| Priority | P0 |
| Type | UI |
| Automated | Yes |

## Preconditions
Seed data loaded (C74). Storage replaced by the in-memory store in automated tests.

## Steps
Open an article with heading, paragraph with BOM, code, PDF.

## Expected Result
Blocks in order; BOM links to the glossary; code has Copy; PDF via a temporary URL.

## Automated coverage
- `frontend/src/pages/knowledge/KnowledgeCenter.test.tsx` — `renders blocks as text, links glossary terms and asks signed-out readers to sign in for feedback`
- `backend/src/test/java/com/vyoog/eisplatform/modules/knowledgebase/service/KnowledgeMediaAndVideoServiceTest.java` — `downloadIsOnlyForReadersWhoMaySeeContentUsingTheFile`

## Actual Result
The automated tests above passed on 2026-10-05.

## Status
Passed (automated)
