# TC-CAT-045: Product content — hosted video: types and size

| Field | Value |
|---|---|
| Test Case ID (required) | TC-CAT-045 |
| Requirement ID (required) | [REQ-CAT-004](../../../docs/02-requirements/FRD/product-content/requirement.md) (REQ-CAT-004.6, BR-PCON-002, BR-PCON-007) — [AC-14](../../../docs/02-requirements/FRD/product-content/acceptance-criteria.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes (`ProductContentServiceTest.aHostedVideoHasItsOwnTypesAndSizeLimit`) |

## Steps
1. Ask for an upload URL for kind VIDEO with an MP4 and a WebM file.
2. Ask again with a MOV, with an MP4 declared as `application/pdf`, and with a file over 500 MB.

## Expected Result
MP4 and WebM get a key under `product-content/{id}/video/` and a 500 MB limit. MOV and the wrong content type are refused (`INVALID_FILE_TYPE`) and the oversize file is refused (`FILE_TOO_LARGE`). The storage information lists `mp4`, `webm` and the 500 MB limit.

## Status
Passed (automated, 2026-10-12).
