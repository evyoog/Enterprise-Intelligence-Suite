# TC-CAT-024: Video links

| Field | Value |
|---|---|
| Test Case ID (required) | TC-CAT-024 |
| Requirement ID (required) | [REQ-CAT-004](../../../docs/02-requirements/FRD/product-content/requirement.md) — [acceptance criteria](../../../docs/02-requirements/FRD/product-content/acceptance-criteria.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An application.

## Steps
1. Add videos with YouTube (watch, youtu.be, embed, shorts), Vimeo, another https site, an http link, a javascript: link and a look-alike host.

## Expected Result
YouTube and Vimeo are recognised (YouTube thumbnail); other https links are plain links with no embed; http, javascript and malformed links are refused (`INVALID_VIDEO_URL`). On the product page the player loads only after Play; plain links open in a new tab with `noopener noreferrer`.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/productcontent/service/ProductVideoLinkTest.java` (all)
- `backend/src/test/java/com/vyoog/eisplatform/modules/productcontent/service/ProductContentServiceTest.java` — `videoLinksAreParsedAndNonHttpsLinksRefused`
- `frontend/src/components/productcontent/ProductContent.test.tsx` — `loads the YouTube player only after the visitor presses play`, `adds a video: validates the link, then saves it as a draft`

## Actual Result
The automated tests above passed on 2026-10-07.

## Status
Passed (automated run 2026-10-07)

## Linked Defect (if failed)
