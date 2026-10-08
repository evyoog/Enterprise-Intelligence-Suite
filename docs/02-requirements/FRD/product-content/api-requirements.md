# API requirements — Product content

Public reads fall under the existing `GET /products/**` rule; administrator calls need `MANAGE_CATALOG` (`/admin/products/**`). Details and payloads: [docs/06-api/api-requirements/product-content.md](../../../06-api/api-requirements/product-content.md).

| Method | Path | Purpose | Permission | Success | Errors |
|---|---|---|---|---|---|
| GET | `/products/{id}/content` | Published content of an Active application, with signed image links | public | 200 | 404 |
| GET | `/products/{id}/content/{itemId}/download` | A 5-minute download link for a published file | public | 200 | 404 |
| GET | `/admin/products/{id}/content` | Every item (draft and published) and the storage limits | `MANAGE_CATALOG` | 200 | 403, 404 |
| POST | `/admin/products/{id}/content/upload-url` | A presigned PUT URL and upload key for one file | `MANAGE_CATALOG` | 200 | 400, 403, 404, 503 |
| POST | `/admin/products/{id}/content` | Add an item | `MANAGE_CATALOG` | 201 | 400, 403, 404, 503 |
| PUT | `/admin/products/{id}/content/{itemId}` | Edit an item, or replace its file with a new upload key | `MANAGE_CATALOG` | 200 | 400, 403, 404, 503 |
| DELETE | `/admin/products/{id}/content/{itemId}` | Delete an item and its files | `MANAGE_CATALOG` | 204 | 403, 404 |
| POST | `/admin/products/{id}/content/{itemId}/publish` · `/unpublish` | Show or hide an item | `MANAGE_CATALOG` | 200 | 400, 403, 404 |
| PUT | `/admin/products/{id}/content/order` | Set the display order | `MANAGE_CATALOG` | 200 | 400, 403, 404 |
| GET | `/admin/products/{id}/content/{itemId}/preview-url` | Signed link to preview a file (also drafts) | `MANAGE_CATALOG` | 200 | 403, 404 |
| GET | `/admin/products/{id}/content/documentation-options` | Knowledge articles that can be linked | `MANAGE_CATALOG` | 200 | 403, 404 |
