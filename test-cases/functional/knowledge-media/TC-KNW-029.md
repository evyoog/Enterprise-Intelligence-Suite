# TC-KNW-029: Deleting a file in use needs confirmation

| Field | Value |
|---|---|
| Test Case ID (required) | TC-KNW-029 |
| Requirement ID (required) | [REQ-KNW-003](../../../docs/02-requirements/FRD/knowledge-media/requirement.md) |
| Acceptance Criterion | [AC-6](../../../docs/02-requirements/FRD/knowledge-media/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-KNW-002](../knowledge-base/TESTPLAN-KNW-002.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
Seed data loaded (C74). Storage replaced by the in-memory store in automated tests.

## Steps
Delete a file used by an article; confirm.

## Expected Result
409 IN_USE lists the article; after confirming the object is deleted and audited.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/knowledgebase/service/KnowledgeMediaAndVideoServiceTest.java` — `downloadIsOnlyForReadersWhoMaySeeContentUsingTheFile`
- `frontend/src/pages/knowledge-admin/KnowledgeManagement.test.tsx` — `asks for confirmation before deleting a file that is in use`

## Actual Result
The automated tests above passed on 2026-10-05.

## Status
Passed (automated)
