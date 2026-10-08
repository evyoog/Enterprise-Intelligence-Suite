# TC-CAT-027: Documentation links only live public articles

| Field | Value |
|---|---|
| Test Case ID (required) | TC-CAT-027 |
| Requirement ID (required) | [REQ-CAT-004](../../../docs/02-requirements/FRD/product-content/requirement.md) — [acceptance criteria](../../../docs/02-requirements/FRD/product-content/acceptance-criteria.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A knowledge article in draft, then published.

## Steps
1. Link the draft article.
2. Publish the article (Public audience) and link it, publish the item.
3. Unpublish the article.

## Expected Result
Step 1 refused (`INVALID_CONTENT`). Step 2 works and the product page links to the article. Step 3: the link disappears from the page and the admin sees a warning on the item.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/productcontent/service/ProductContentServiceTest.java` — `documentationLinksOnlyLivePublicArticlesAndDisappearsWhenTheArticleDoes`
- `frontend/src/components/productcontent/ProductContent.test.tsx` — `links documentation from the live articles`

## Actual Result
The automated tests above passed on 2026-10-07.

## Status
Passed (automated run 2026-10-07)

## Linked Defect (if failed)
