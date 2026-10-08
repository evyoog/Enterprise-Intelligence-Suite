# Integration — AWS S3 (knowledge media)

**Status:** Built 2026-10-05 ([C78](../01-business/roadmap/open-decisions.md#c78)); defined 2026-10-05, [C72](../01-business/roadmap/open-decisions.md#c72), D23 → A). Used by [REQ-KNW-003](../02-requirements/FRD/knowledge-media/requirement.md) and [REQ-KNW-004](../02-requirements/FRD/knowledge-videos/requirement.md). Rules: [BR-MED-001](../03-business-rules/BR-MED-001-private-media-storage.md), [BR-SEC-001](../03-business-rules/BR-SEC-001-central-secrets-file.md).

## Bucket
| Setting | Value |
|---|---|
| Name | From configuration `eis.knowledge.storage.bucket` (for example `eis-knowledge-production`); one bucket per environment — names **Not specified** (open question) |
| Region | `eis.knowledge.storage.region` — **Not specified** |
| Block Public Access | **On** (all four settings) |
| Object Ownership | Bucket owner enforced (ACLs disabled) |
| Versioning | On (protects against accidental overwrite and delete; old versions expire by lifecycle) |
| Default encryption | SSE-S3 (proposed; SSE-KMS if required — confirm) |
| CORS | Allowed origins: the EIS web origins only (`app.cors-allowed-origins`); methods PUT, GET, HEAD; headers `Content-Type`, `Content-Length`, `x-amz-*`; exposed `ETag`; max age 3000 s |

## Prefixes and keys
```
videos/{tutorials|training|troubleshooting|webinars|demos}/{product}/{module}/{uuid}.{ext}
images/{screenshots|diagrams|thumbnails}/{product}/{module}/{uuid}.{ext}
documents/{manuals|guides|brochures|release-notes}/{product}/{module}/{uuid}.{ext}
templates/{product}/{module}/{uuid}.{ext}
product-content/{applicationId}/{datasheet|image|case_study|case_study-logo}/{uuid}.{ext}   (REQ-CAT-004, C81: product datasheets, images, case-study files; same bucket and storage service)
audio/{product}/{module}/{uuid}.{ext}
transcripts/{video-uuid}/{language}.vtt
uploads-pending/   (never used: pending objects live at their final key; the database marks them PENDING)
```
Example: `videos/tutorials/valam/inventory/3f2a…c9.mp4`. `{product}` and `{module}` are taxonomy slugs; the original file name is stored only as metadata.

## IAM
- Prefer an **IAM role** (ECS task role on AWS). Otherwise static keys **only** in `config/secrets.env` on the server (`EIS_KNOWLEDGE_AWS_ACCESS_KEY_ID`, `EIS_KNOWLEDGE_AWS_SECRET_ACCESS_KEY`), never in the repository, frontend or logs.
- Policy (least privilege), on `arn:aws:s3:::<bucket>/videos/*`, `images/*`, `documents/*`, `templates/*`, `audio/*`, `transcripts/*`: `s3:PutObject`, `s3:GetObject`, `s3:DeleteObject`, `s3:AbortMultipartUpload`, `s3:ListMultipartUploadParts`; on the bucket: `s3:ListBucket` (cleanup job), `s3:ListBucketMultipartUploads`. No `s3:PutObjectAcl`, no bucket-policy rights.

## Presigned URLs
| URL | Method | Expiry setting | Default (proposed — confirm) |
|---|---|---|---|
| Upload | PUT (or multipart part PUTs) | `eis.knowledge.upload-url-expiry` | 15 minutes |
| Download | GET | `eis.knowledge.download-url-expiry` | 5 minutes |
| Playback | GET | `eis.knowledge.playback-url-expiry` | 2 hours (covers a long video; the player asks again when it expires) |
Upload URLs are signed with the exact `Content-Type` and `Content-Length`, so S3 refuses a different file.

## Lifecycle and retention
- Noncurrent versions: expire after `eis.knowledge.retention.noncurrent-days` (**Not specified** — open question).
- Incomplete multipart uploads: abort after 1 day.
- Orphan cleanup job (backend): PENDING items older than `eis.knowledge.pending-max-age` (proposed 24 h) and objects without a record are deleted.

## Configuration (`eis.knowledge.*`, environment overrides)
| Key | Environment variable | Meaning |
|---|---|---|
| `eis.knowledge.storage.provider` | `EIS_KNOWLEDGE_STORAGE_PROVIDER` | `s3` (or `local` for development) |
| `eis.knowledge.storage.bucket` | `EIS_KNOWLEDGE_BUCKET` | Bucket name |
| `eis.knowledge.storage.region` | `EIS_KNOWLEDGE_REGION` | AWS region |
| `eis.knowledge.video.max-size` | `EIS_KNOWLEDGE_VIDEO_MAX_SIZE` | Maximum video size (default 5 GB, C78) |
| `eis.knowledge.document.max-size` | `EIS_KNOWLEDGE_DOCUMENT_MAX_SIZE` | Maximum document size (default 100 MB, C78) |
| `eis.knowledge.image.max-size` | `EIS_KNOWLEDGE_IMAGE_MAX_SIZE` | Maximum image size (default 10 MB) |
| `eis.knowledge.audio.max-size` | `EIS_KNOWLEDGE_AUDIO_MAX_SIZE` | Maximum audio size (default 100 MB) |
| `eis.knowledge.storage.endpoint` | `EIS_KNOWLEDGE_STORAGE_ENDPOINT` | Optional S3-compatible endpoint (for example MinIO in development) |
| `eis.knowledge.orphan-after` | `EIS_KNOWLEDGE_ORPHAN_AFTER` | Unfinished uploads removed after (default PT24H) |
| `eis.knowledge.jobs.enabled`, `eis.knowledge.seed.enabled` | `EIS_KNOWLEDGE_JOBS_ENABLED`, `EIS_KNOWLEDGE_SEED_ENABLED` | Scheduled publish/cleanup jobs; seed data on first start |
| `eis.knowledge.multipart-threshold` | `EIS_KNOWLEDGE_MULTIPART_THRESHOLD` | Proposed 100 MB |
| `eis.knowledge.upload-url-expiry`, `download-url-expiry`, `playback-url-expiry` | `EIS_KNOWLEDGE_*_URL_EXPIRY` | See above |
Secrets (only `config/secrets.env`, template empty): `EIS_KNOWLEDGE_AWS_ACCESS_KEY_ID`, `EIS_KNOWLEDGE_AWS_SECRET_ACCESS_KEY` (omit with an IAM role). The `spring.datasource` block is not touched.

## Failure handling
S3 errors are logged on the server with the request id; users see friendly messages (REQ-KNW-003.11). If S3 is unavailable, uploads and S3 playback fail gracefully; YouTube and external videos and all text content keep working.

## As built (2026-10-05)
- `S3MediaStorageService` (AWS SDK v2, server side only): presigned PUT / multipart parts / GET, HEAD verification, delete. Static keys are used only when both `EIS_KNOWLEDGE_AWS_ACCESS_KEY_ID` and `EIS_KNOWLEDGE_AWS_SECRET_ACCESS_KEY` are set; otherwise the default AWS chain (IAM role).
- `eis.knowledge.storage.provider` defaults to `none`: uploads then answer STORAGE_NOT_CONFIGURED and hosted playback is unavailable; YouTube and external videos still work.
- Browser uploads send `Content-Type` as signed; CORS must allow `PUT`, `GET` and expose `ETag` (multipart completion needs it).
- Verified: presigned URLs carry `X-Amz-Expires` and never the secret (unit test with the real signer); no credentials in API responses or the built frontend bundle. An expired URL refusing at S3 needs a real bucket (UAT-KNW-003).
