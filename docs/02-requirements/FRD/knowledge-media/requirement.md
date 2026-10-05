# REQ-KNW-003 — Knowledge media library and storage

**Status:** Draft — waits for "Approved"
**Owner:** Product owner
**Decisions:** [C72](../../../01-business/roadmap/open-decisions.md#c72) (D23 → A, AWS S3)

| Field | Value |
|---|---|
| Sprint | [2027.1.1](../../../01-business/roadmap/sprints/SPRINT-2027.1.1.md) (planned; dates unchanged) |
| Requirement ID | REQ-KNW-003 |
| Application | [11 Training & Knowledge Management](../../../01-business/roadmap/applications/11-training-knowledge-management.md) |
| Priority | P0 |
| Integration | [aws-s3.md](../../../09-integrations/aws-s3.md) |

## Summary
A media library for knowledge files (images, documents, PDFs, audio, templates; videos use the same storage through REQ-KNW-004), stored in a private S3 bucket. The browser uploads directly to S3 with a presigned URL; readers get files through short-lived presigned URLs after an authorization check. A storage abstraction keeps S3 behind one interface.

## Functional requirements
| ID | Requirement | Priority |
|---|---|---|
| REQ-KNW-003.1 | `MediaStorageService` with `generateUploadUrl()`, `completeUpload()`, `getTemporaryUrl()`, `delete()`, `replace()`, `getMetadata()`; implementation `S3MediaStorageService` (AWS SDK v2); prepared for `LocalMediaStorageService`. The frontend never uses the AWS SDK. | Must |
| REQ-KNW-003.2 | **Upload:** the browser sends metadata only (file name, content type, size, media kind, product, module); the backend checks permission (contributor), allowed type, size (from configuration), product and module; it returns a presigned PUT URL for a new unique object key (never the file name) with a short expiry; the browser uploads to S3 with progress and cancel; on completion the backend checks the object exists (HEAD) and its size and type match, then stores the media item. | Must |
| REQ-KNW-003.3 | **Multipart** upload for files above a configured threshold (proposed 100 MB — confirm), with presigned part URLs and complete/abort calls. | Should |
| REQ-KNW-003.4 | **Allowed types** (proposed — confirm): images PNG, JPG, WebP, SVG (sanitised), GIF; documents PDF, DOC/DOCX, XLS/XLSX, PPT/PPTX, CSV, TXT; audio MP3, M4A, WAV; templates as documents; videos per REQ-KNW-004. Type is checked by extension and declared content type, and after upload by the stored object's content type. Maximum sizes per kind come from `eis.knowledge.*` configuration. | Must |
| REQ-KNW-003.5 | **Library:** upload, preview, search, filter (images, videos, audio, documents, PDF, templates), replace, delete, reuse in content. Columns: thumbnail, file name, type, size, uploaded date, used by, version. | Must |
| REQ-KNW-003.6 | **Delete** warns and lists every content item using the file; deleting removes the S3 object, the record and its search entry, and is audited. | Must |
| REQ-KNW-003.7 | **Replace:** upload a new file → verify → the media item gets a new version pointing to the new object; the old object is kept or deleted by the retention policy (open question). Content using the item shows the new file. | Must |
| REQ-KNW-003.8 | **Download/view:** `GET …/download-url` authenticates, authorises (BR-KVS-001 on the content using the file, or publisher) and returns a presigned GET URL (expiry from configuration). Downloads are counted (REQ-KNW-006). | Must |
| REQ-KNW-003.9 | **Orphan cleanup:** a scheduled job deletes objects whose upload never completed after a configured time, and objects of deleted items still present. | Must |
| REQ-KNW-003.10 | **Storage information** (publishers only): provider, bucket, region, object path, size, format, upload status — never credentials. | Must |
| REQ-KNW-003.11 | Friendly errors for invalid format, too large, network failure, S3 failure, expired upload URL, permission denied, AWS unavailable, duplicate upload, cancelled upload, database failure; details logged on the server only. | Must |
| REQ-KNW-003.12 | Text extracted from documents (and OCR text from images, if available) is indexed for search (C76); extraction service **Not specified** — an integration point (`TextExtractionService`) is prepared. | Should |

## Out of scope
- Moving product and platform images (`ProductImageService`) to S3 — Not specified (C72).
- Virus scanning — Not specified (open question).

## Open questions
| # | Question | Blocks approval |
|---|---|---|
| 1 | Maximum upload sizes per kind (videos, documents, images, audio) and presigned URL expiries (upload, download, playback). | Yes |
| 2 | Retention policy for replaced and deleted files (keep old versions how long?). | Yes |
| 3 | Bucket names and region per environment (development, test, production). | Yes |
| 4 | Multipart threshold (proposed 100 MB). | No — confirm in review |
| 5 | Allowed file types (proposed list above). | No — confirm in review |
| 6 | Virus/malware scanning of uploads: needed? which service? | No — confirm in review |
