# TC-KNW-002: Publishing makes an article publicly searchable and readable

| Field | Value |
|---|---|
| Test Case ID (required) | TC-KNW-002 |
| Requirement ID (required) | [REQ-KNW-001](../../../docs/02-requirements/FRD/knowledge-base/requirement.md) |
| Acceptance Criterion | [AC-2](../../../docs/02-requirements/FRD/knowledge-base/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-KNW-001](TESTPLAN-KNW-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A DRAFT article.

## Steps
1. Arrange: create an article.
2. Act: publish it.
3. Observe public search and the public read.

## Expected Result
The article now appears in a matching public search, and its detail is readable.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/knowledgebase/service/KnowledgeArticleServiceTest.java` — `publishingMakesItSearchableThenUnpublishingHidesItAgain`

## Actual Result
The automated tests below passed on 2026-09-28.

## Status
Passed (automated run 2026-09-28)

## Linked Defect (if failed)
None.
