# TC-KNW-005: Search matches title or body, case-insensitively

| Field | Value |
|---|---|
| Test Case ID (required) | TC-KNW-005 |
| Requirement ID (required) | [REQ-KNW-001](../../../docs/02-requirements/FRD/knowledge-base/requirement.md) |
| Acceptance Criterion | [AC-5](../../../docs/02-requirements/FRD/knowledge-base/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-KNW-001](TESTPLAN-KNW-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A published article.

## Steps
1. Arrange: create and publish an article with distinct title/body text.
2. Act: search using an upper-case fragment of the title, then a fragment of the body, then a non-matching term.
3. Observe each result set.

## Expected Result
Both matching searches return the article; the non-matching search does not.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/knowledgebase/service/KnowledgeArticleServiceTest.java` — `searchMatchesTitleOrBodyCaseInsensitively`

## Actual Result
The automated tests below passed on 2026-09-28.

## Status
Passed (automated run 2026-09-28)

## Linked Defect (if failed)
None.
