# TC-CAT-023: Downloads are short-lived and nothing is permanent

| Field | Value |
|---|---|
| Test Case ID (required) | TC-CAT-023 |
| Requirement ID (required) | [REQ-CAT-004](../../../docs/02-requirements/FRD/product-content/requirement.md) — [acceptance criteria](../../../docs/02-requirements/FRD/product-content/acceptance-criteria.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A published datasheet and image.

## Steps
1. Open the content as a visitor, click Download.
2. Try to download a draft, an image, another application's item.
3. Read every response for credentials or bucket addresses.

## Expected Result
A 5-minute signed link on click; images use 1-hour links; drafts, images and other applications' items answer 404; no response holds a credential, secret or bucket name.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/productcontent/service/ProductContentServiceTest.java` — `downloadsAreShortLivedAndNeverForDraftsOrWrongProducts`, `imagesUseAOneHourLinkAndNoResponseHoldsACredentialOrABucketAddress`
- `frontend/src/components/productcontent/ProductContent.test.tsx` — `asks for a fresh download link only when the visitor clicks`, `says so when a download cannot start`

## Actual Result
The automated tests above passed on 2026-10-07.

## Status
Passed (automated run 2026-10-07)

## Linked Defect (if failed)
