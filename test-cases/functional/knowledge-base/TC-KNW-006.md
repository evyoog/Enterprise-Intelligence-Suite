# TC-KNW-006: Deleting an article removes it from the admin list

| Field | Value |
|---|---|
| Test Case ID (required) | TC-KNW-006 |
| Requirement ID (required) | [REQ-KNW-001](../../../docs/02-requirements/FRD/knowledge-base/requirement.md) |
| Acceptance Criterion | [AC-6](../../../docs/02-requirements/FRD/knowledge-base/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-KNW-001](TESTPLAN-KNW-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An existing article.

## Steps
1. Arrange: create an article.
2. Act: delete it.
3. Act: attempt to read it via the admin get-by-id call.

## Expected Result
The admin read is refused (404).

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/knowledgebase/service/KnowledgeArticleServiceTest.java` — `deletingAnArticleRemovesItFromTheAdminList`

## Actual Result
The automated tests below passed on 2026-09-28.

## Status
Passed (automated run 2026-09-28)

## Linked Defect (if failed)
None.
