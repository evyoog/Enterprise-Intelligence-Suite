# TC-KNW-004: Editing increments the version counter

| Field | Value |
|---|---|
| Test Case ID (required) | TC-KNW-004 |
| Requirement ID (required) | [REQ-KNW-001](../../../docs/02-requirements/FRD/knowledge-base/requirement.md) |
| Acceptance Criterion | [AC-4](../../../docs/02-requirements/FRD/knowledge-base/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-KNW-001](TESTPLAN-KNW-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An existing article.

## Steps
1. Arrange: create an article (version 1).
2. Act: edit its title and body.
3. Observe the returned version.

## Expected Result
The version becomes 2, and the title/body reflect the edit.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/knowledgebase/service/KnowledgeArticleServiceTest.java` — `editingIncrementsTheVersionCounter`

## Actual Result
The automated tests below passed on 2026-09-28.

## Status
Passed (automated run 2026-09-28)

## Linked Defect (if failed)
None.
