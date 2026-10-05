# Workflow — Video (and media) upload to S3

**Status:** Built 2026-10-05 ([C78](../01-business/roadmap/open-decisions.md#c78)); defined 2026-10-05). [REQ-KNW-003](../02-requirements/FRD/knowledge-media/requirement.md), [REQ-KNW-004](../02-requirements/FRD/knowledge-videos/requirement.md), [aws-s3.md](../09-integrations/aws-s3.md).

```mermaid
sequenceDiagram
  actor A as Contributor (browser)
  participant B as EIS backend
  participant S as S3 (private bucket)
  A->>B: POST /knowledge/videos/upload-url {fileName, contentType, size, productId, moduleId, kind}
  B->>B: permission, type, size, product, module checks
  B->>B: create PENDING media item, key videos/tutorials/{product}/{module}/{uuid}.mp4
  B-->>A: {uploadUrl, objectKey, expiresAt} (or multipart part URLs)
  A->>S: PUT file (progress, speed, Cancel)
  S-->>A: 200 ETag
  A->>B: POST /knowledge/videos/{id}/complete-upload {objectKey, etag}
  B->>S: HEAD object
  S-->>B: size, content type
  B->>B: verify → READY, store metadata, audit VIDEO_UPLOADED
  B-->>A: video record
  Note over A,S: Cancel → abort (multipart) and the item is deleted; failure → FAILED, audit VIDEO_UPLOAD_FAILED
  Note over B,S: Cleanup job deletes PENDING items older than the limit
```
