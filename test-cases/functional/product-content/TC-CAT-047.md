# TC-CAT-047: Product content — hosted video: admin dialog and product page player

| Field | Value |
|---|---|
| Test Case ID (required) | TC-CAT-047 |
| Requirement ID (required) | [REQ-CAT-004](../../../docs/02-requirements/FRD/product-content/requirement.md) (REQ-CAT-004.6, BR-PCON-002, BR-PCON-007) — [AC-14](../../../docs/02-requirements/FRD/product-content/acceptance-criteria.md) |
| Priority | P1 |
| Type | Functional (UI) |
| Automated | Yes (`ProductContent.test.tsx`); the real-bucket part is manual |

## Steps
1. In the Content tab choose Add video, switch to "Upload a video file", choose an MP4: it uploads with progress and Save attaches it.
2. Choose a MOV; switch back to a link; turn storage off.
3. Open the product's Resources tab with a published hosted video.
4. Manual, with a real bucket: upload a real MP4 of about 50 MB, publish it, play it and seek on the product page; wait for the link to expire and reload.

## Expected Result
The file option sends `kind: VIDEO`, no link and the verified upload; a MOV is refused before uploading (Allowed: MP4, WEBM); with storage off the file option is disabled and a link still works. The Resources tab shows a native video player with controls and the video's title, with no accessibility violation. With a real bucket the video plays and seeks, and a reload after expiry gets a fresh link.

## Status
Automated part passed (2026-10-12). Real-bucket part: Not run (no test bucket in the build environment).
