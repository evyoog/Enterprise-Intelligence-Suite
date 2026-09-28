# TC-KNW-001: A new article starts as draft and is invisible to the public

| Field | Value |
|---|---|
| Test Case ID (required) | TC-KNW-001 |
| Requirement ID (required) | [REQ-KNW-001](../../../docs/02-requirements/FRD/knowledge-base/requirement.md) |
| Acceptance Criterion | [AC-1](../../../docs/02-requirements/FRD/knowledge-base/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-KNW-001](TESTPLAN-KNW-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
None.

## Steps
1. Act: create an article.
2. Observe its status/version.
3. Act: search published articles for its title; attempt to read it publicly.

## Expected Result
The article is DRAFT with version 1; it is absent from search results and the public read is refused (404).

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/knowledgebase/service/KnowledgeArticleServiceTest.java` — `createdArticleStartsAsDraftAndIsInvisibleToPublicSearch`

## Actual Result
The automated tests below passed on 2026-09-28.

## Status
Passed (automated run 2026-09-28)

## Linked Defect (if failed)
None.
