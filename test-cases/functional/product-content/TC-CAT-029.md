# TC-CAT-029: Without storage, links still work

| Field | Value |
|---|---|
| Test Case ID (required) | TC-CAT-029 |
| Requirement ID (required) | [REQ-CAT-004](../../../docs/02-requirements/FRD/product-content/requirement.md) — [acceptance criteria](../../../docs/02-requirements/FRD/product-content/acceptance-criteria.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
File storage not configured (provider none).

## Steps
1. Ask for an upload URL.
2. Add a video and a case study without files.
3. Open the Content tab.

## Expected Result
503 `STORAGE_NOT_CONFIGURED` with a friendly message; links and case studies without files save; the tab shows the notice and disables Add datasheet and Add image.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/productcontent/service/ProductContentServiceTest.java` — `withoutStorageFilesAreRefusedButLinksStillWork`
- `frontend/src/components/productcontent/ProductContent.test.tsx` — `without storage the file sections cannot add, but videos and documentation still can`

## Actual Result
The automated tests above passed on 2026-10-07.

## Status
Passed (automated run 2026-10-07)

## Linked Defect (if failed)
