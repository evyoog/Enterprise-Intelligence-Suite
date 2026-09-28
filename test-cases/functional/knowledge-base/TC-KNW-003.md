# TC-KNW-003: Unpublishing hides an article again

| Field | Value |
|---|---|
| Test Case ID (required) | TC-KNW-003 |
| Requirement ID (required) | [REQ-KNW-001](../../../docs/02-requirements/FRD/knowledge-base/requirement.md) |
| Acceptance Criterion | [AC-3](../../../docs/02-requirements/FRD/knowledge-base/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-KNW-001](TESTPLAN-KNW-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A PUBLISHED article.

## Steps
1. Arrange: create and publish an article.
2. Act: unpublish it.
3. Observe public search and the public read.

## Expected Result
The article disappears from public search and its detail read is refused (404) again.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/knowledgebase/service/KnowledgeArticleServiceTest.java` — `publishingMakesItSearchableThenUnpublishingHidesItAgain`

## Actual Result
The automated tests below passed on 2026-09-28.

## Status
Passed (automated run 2026-09-28)

## Linked Defect (if failed)
None.
