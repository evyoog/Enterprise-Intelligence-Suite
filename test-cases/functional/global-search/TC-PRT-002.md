# TC-PRT-002: A matching published knowledge article appears in results

| Field | Value |
|---|---|
| Test Case ID (required) | TC-PRT-002 |
| Requirement ID (required) | [REQ-PRT-002](../../../docs/02-requirements/FRD/global-search/requirement.md) |
| Acceptance Criterion | [AC-2](../../../docs/02-requirements/FRD/global-search/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-PRT-002](TESTPLAN-PRT-002.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A PUBLISHED knowledge article with a distinctive title.

## Steps
1. Arrange: create and publish a knowledge article.
2. Act: call search(title, null, null).
3. Observe the knowledgeArticles list.

## Expected Result
The article appears in the knowledgeArticles list.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/search/service/GlobalSearchServiceTest.java` — `findsAMatchingPublishedKnowledgeArticle`

## Actual Result
The automated tests below passed on 2026-09-28.

## Status
Passed (automated run 2026-09-28)

## Linked Defect (if failed)
None.
