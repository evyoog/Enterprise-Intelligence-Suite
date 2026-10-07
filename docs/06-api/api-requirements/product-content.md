# API — Product content (02.04)

**Status:** Built 2026-10-07 ([C81](../../01-business/roadmap/open-decisions.md#c81)) for [REQ-CAT-004](../../02-requirements/FRD/product-content/requirement.md). Controllers: `ProductContentController` (public reads, under the existing `GET /products/**` permitAll rule) and `AdminProductContentController` (`/admin/products/{productId}/content/**`, platform permission `MANAGE_CATALOG`). Conventions as the other EIS APIs: context path `/api` (shown without it); errors `{ timestamp, status, error, message, code? }` with the stable codes `INVALID_FILE_TYPE`, `FILE_TOO_LARGE`, `UPLOAD_NOT_FOUND`, `UPLOAD_MISMATCH`, `DUPLICATE_UPLOAD`, `STORAGE_NOT_CONFIGURED`, `INVALID_VIDEO_URL`, `INVALID_CONTENT`.

Files never pass through these endpoints: the browser uploads to the private S3 bucket with the presigned URL from `upload-url` (same bucket and storage service as the knowledge media, [aws-s3.md](../../09-integrations/aws-s3.md)). No response holds a credential or a permanent file URL.

## Public (everyone)
| Method | Path | Purpose |
|---|---|---|
| GET | `/products/{id}/content` | Published items of an **Active** application, grouped: `datasheets`, `documentation`, `images`, `videos`, `caseStudies`. Images carry a signed link valid 1 hour; files carry only name, size and type. 404 for an unknown or non-Active application. |
| GET | `/products/{id}/content/{itemId}/download` | `{ url, expiresAt, fileName, size }`: a 5-minute signed download link for a published datasheet or case-study PDF. 404 for a draft item, an image or another application's item. |

## Administrator (`MANAGE_CATALOG`)
| Method | Path | Purpose |
|---|---|---|
| GET | `/admin/products/{id}/content` | `{ items[], storage{configured, imageMaxSize, documentMaxSize, imageTypes, documentTypes} }`: every item (drafts included), in display order, with previews for images and logos |
| POST | `/admin/products/{id}/content/upload-url` | Body `{ kind, purpose?: "file"\|"logo", fileName, contentType, size }`. Checks the type and size and returns `{ uploadKey, uploadUrl, expiresAt, maxSize }`: a presigned PUT for a new key `product-content/{id}/{kind}/{uuid}.{ext}`. 400 `INVALID_FILE_TYPE` / `FILE_TOO_LARGE`; 503 `STORAGE_NOT_CONFIGURED` |
| POST | `/admin/products/{id}/content` | Add an item (starts as a Draft, version 1). Body below. 201 |
| PUT | `/admin/products/{id}/content/{itemId}` | Edit an item. A new `file` or `logo` replaces the old one (the old object is deleted). The version rises by one when anything changed. The kind cannot change |
| DELETE | `/admin/products/{id}/content/{itemId}` | Delete the item and its files. 204 |
| POST | `/admin/products/{id}/content/{itemId}/publish` · `/unpublish` | Show or hide the item |
| PUT | `/admin/products/{id}/content/order` | Body `{ ids: [...] }`: display order (items not listed go last) |
| GET | `/admin/products/{id}/content/{itemId}/preview-url?logo=false` | A signed link to look at a file, drafts included |
| GET | `/admin/products/{id}/content/documentation-options` | Live knowledge articles visible to everyone that can be linked (this application's first) |

### Item body
```json
{
  "kind": "DATASHEET | DOCUMENTATION | IMAGE | VIDEO | CASE_STUDY",
  "title": "Platform datasheet",
  "description": "optional",
  "altText": "IMAGE only, required",
  "videoUrl": "VIDEO only: https link (YouTube and Vimeo recognised)",
  "customerName": "CASE_STUDY only, required", "problem": "…", "result": "…",
  "articleId": 40,
  "file": { "uploadKey": "product-content/7/datasheet/….pdf", "fileName": "sheet.pdf", "contentType": "application/pdf", "size": 2048 },
  "logo": { "uploadKey": "…", "fileName": "acme.png", "contentType": "image/png", "size": 800 }
}
```
On save the backend checks that the key belongs to this application and kind, that the object exists in storage, and that its size and content type match `file` (otherwise the object is deleted and `UPLOAD_MISMATCH` is returned).

## Settings
`eis.product-content.image-max-size` (5 MB), `document-max-size` (20 MB), `upload-url-expiry` (15 min), `download-url-expiry` (5 min), `view-url-expiry` (1 h); environment overrides `EIS_PRODUCT_CONTENT_*`.

## Audit actions
`PRODUCT_CONTENT_ADDED`, `_UPDATED`, `_PUBLISHED`, `_UNPUBLISHED`, `_DELETED`, `_REORDERED`.
