# TC-CAT-046: Product content — hosted video: link or file, play and switch

| Field | Value |
|---|---|
| Test Case ID (required) | TC-CAT-046 |
| Requirement ID (required) | [REQ-CAT-004](../../../docs/02-requirements/FRD/product-content/requirement.md) (REQ-CAT-004.6, BR-PCON-002, BR-PCON-007) — [AC-14](../../../docs/02-requirements/FRD/product-content/acceptance-criteria.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes (`ProductContentServiceTest`: `anUploadedVideoIsStoredAsAFileAndPlayedFromAShortLivedLink`, `aVideoIsAFileOrALinkNeverBothAndNeverNeither`, `switchingAVideoBetweenALinkAndAFileDropsTheOldOneAndRaisesTheVersion`) |

## Steps
1. Create a video from an uploaded MP4, publish it and read the public content.
2. Create a video with both a link and a file, and one with neither.
3. Edit the title only; change the video to a YouTube link; change it back to a WebM file; delete it.

## Expected Result
The hosted video has provider `HOSTED`, no link, its file info and a 2-hour play link (public only once published); it has no download (404). Both or neither is refused (`INVALID_VIDEO_URL`). A title-only edit keeps the file; switching deletes the old file, clears the other fields and raises the version each time; deleting the item deletes the stored video.

## Status
Passed (automated, 2026-10-12).
