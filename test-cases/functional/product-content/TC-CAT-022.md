# TC-CAT-022: Draft, publish and unpublish control what visitors see

| Field | Value |
|---|---|
| Test Case ID (required) | TC-CAT-022 |
| Requirement ID (required) | [REQ-CAT-004](../../../docs/02-requirements/FRD/product-content/requirement.md) — [acceptance criteria](../../../docs/02-requirements/FRD/product-content/acceptance-criteria.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An application with a draft datasheet and a draft video.

## Steps
1. Open the product's content as a visitor.
2. Publish both, open it again.
3. Unpublish the datasheet.
4. Retire the application and open it again.

## Expected Result
Nothing at first; after Publish both show (Vimeo with its embedded player address); after Unpublish the datasheet is gone; a non-Active application answers 404 for the content and for downloads.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/productcontent/service/ProductContentServiceTest.java` — `visitorsSeeOnlyPublishedItemsOfAnActiveProduct`, `contentIsHiddenWhileTheProductIsNotActive`
- `frontend/src/pages/ProductDetailPage.test.tsx` — `shows a Resources tab only when the product has published content`, `has no Resources tab when nothing is published`

## Actual Result
The automated tests above passed on 2026-10-07.

## Status
Passed (automated run 2026-10-07)

## Linked Defect (if failed)
