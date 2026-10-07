# TC-CAT-020: Wrong types and oversized files are refused

| Field | Value |
|---|---|
| Test Case ID (required) | TC-CAT-020 |
| Requirement ID (required) | [REQ-CAT-004](../../../docs/02-requirements/FRD/product-content/requirement.md) — [acceptance criteria](../../../docs/02-requirements/FRD/product-content/acceptance-criteria.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
As TC-CAT-019.

## Steps
1. Ask for upload URLs for SVG, GIF, EXE, a PDF declared as an image, an image declared as a PDF, a 5 MB + 1 image, a 20 MB + 1 PDF, an upload for a video item and a logo for a datasheet.

## Expected Result
400 `INVALID_FILE_TYPE`, `FILE_TOO_LARGE` or `INVALID_CONTENT` with a friendly message; no URL issued. The admin UI refuses a wrong type or size before uploading anything.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/productcontent/service/ProductContentServiceTest.java` — `refusesWrongTypesAndTooLargeFilesWithAFriendlyCode`
- `frontend/src/components/productcontent/ProductContent.test.tsx` — `refuses a wrong file type or an oversized file before uploading anything`

## Actual Result
The automated tests above passed on 2026-10-07.

## Status
Passed (automated run 2026-10-07)

## Linked Defect (if failed)
