# REQ-CAT-004 — Product content (datasheets, documentation, images, videos, case studies)

**Status:** Approved (2026-10-07, [C81](../../../01-business/roadmap/open-decisions.md#c81)) — built in sprint 2026.4.1
**Owner:** Product owner
**Decisions:** [C81](../../../01-business/roadmap/open-decisions.md#c81) (PC-1 to PC-6 answered with the recommended defaults), [C72](../../../01-business/roadmap/open-decisions.md#c72) (D23, private AWS S3)

| Field | Value |
|---|---|
| Sprint | [2026.4.1](../../../01-business/roadmap/sprints/SPRINT-2026.4.1.md) |
| Requirement ID | REQ-CAT-004 |
| Application | [02 Product & Catalog Management](../../../01-business/roadmap/applications/02-product-catalog-management.md#0204-product-content) |
| Functions | 02.04.01.01 Upload datasheet, 02.04.01.02 Publish documentation, 02.04.01.03 Version content, 02.04.02.01 Upload images, 02.04.02.02 Upload videos, 02.04.02.03 Manage case studies |
| Priority | P0 |
| Integration | [aws-s3.md](../../../09-integrations/aws-s3.md) |

## Summary
A platform administrator adds the material that helps a customer decide to buy to each **application** (catalog product): a **datasheet** (PDF), **documentation** (links to published knowledge-base articles, besides the existing documentation link), **images** (a gallery), **videos** (a YouTube, Vimeo or other web link) and **case studies**. It is configured in the application's edit screen (a **Content** tab next to the existing details form: name, logo, features, resources, pricing). Files are uploaded straight from the browser to the private S3 bucket (REQ-KNW-003 storage); customers see the published items in a **Resources** tab on the product page and get files through short-lived signed links.

## Actors
- **Platform administrator** (`MANAGE_CATALOG`): configures and publishes content.
- **Visitor or customer**: reads published content on the product page.

## Functional requirements
| ID | Requirement | Priority |
|---|---|---|
| REQ-CAT-004.1 | An application has content items of five kinds: DATASHEET, DOCUMENTATION, IMAGE, VIDEO, CASE_STUDY. Each has a title, an optional description, a status (Draft or Published), a display order, a version and an "Updated on" date. | Must |
| REQ-CAT-004.2 | **Upload datasheet (02.04.01.01):** a PDF up to 20 MB, uploaded directly to S3 (presigned PUT, no file through the EIS backend). The backend checks the permission, extension, content type and size before issuing the URL, and after the upload checks that the object exists and that its size and type match. | Must |
| REQ-CAT-004.3 | **Publish documentation (02.04.01.02):** documentation is written as knowledge-base articles (REQ-KNW) and linked to the application; only live articles visible to everyone (audience Public) can be linked, and a linked article that stops being live or public disappears from the product page by itself. The existing external **Documentation URL** and **Support URL** (Resources section of the form) stay as extra links. | Must |
| REQ-CAT-004.4 | **Version content (02.04.01.03):** only the latest version of an item is kept. Editing an item or replacing its file raises its version by one and sets "Updated on"; the product page shows both ("Version 3, updated 7 Oct 2026"). The replaced file is deleted from storage. | Must |
| REQ-CAT-004.5 | **Upload images (02.04.02.01):** PNG, JPG or WebP up to 5 MB, with a required text alternative (alt text). SVG and GIF are not accepted (SVG cannot be sanitised because files do not pass through the backend). Images form a gallery in the display order. | Must |
| REQ-CAT-004.6 | **Upload videos (02.04.02.02):** a video is a web link, not an uploaded file. YouTube and Vimeo links are recognised (the product page plays them in a privacy-friendly embedded player, and YouTube gets its thumbnail); any other `https` link is shown as a link that opens in a new tab. Hosting and streaming video files is out of scope here (use the knowledge-base video library). | Must |
| REQ-CAT-004.7 | **Manage case studies (02.04.02.03):** a case study has a customer name, a title, the problem, the result, an optional customer logo (image) and an optional PDF (up to 20 MB). | Must |
| REQ-CAT-004.8 | Content is configured on the application's edit screen: add, edit, replace the file, delete (with confirmation), reorder (move up and down), publish and unpublish. A new item starts as a Draft. Draft items are never shown to customers. | Must |
| REQ-CAT-004.9 | The public product page has a **Resources** tab (shown when the application has any published content) with the datasheet download, documentation links, image gallery, videos and case studies. Content is hidden while the application is not Active. | Must |
| REQ-CAT-004.10 | Files are never reachable by a permanent URL: images are shown through a signed link valid for one hour, downloads through a signed link valid for 5 minutes created when the visitor clicks. No credential or bucket secret appears in any response, page or script. | Must |
| REQ-CAT-004.11 | If file storage is not configured, uploads answer `STORAGE_NOT_CONFIGURED` (503) with a friendly message; links (videos, documentation) and case studies without files still work. | Must |
| REQ-CAT-004.12 | Deleting an item deletes its files from storage; deleting an application deletes all its content and files. | Must |
| REQ-CAT-004.13 | Adding, changing, replacing, publishing, unpublishing, reordering and deleting content are audited. | Must |
| REQ-CAT-004.14 | Every screen text uses i18n (English and Spanish), has keyboard access and screen-reader names, and passes the accessibility checks. | Must |

## Answers applied on 2026-10-07 ([C81](../../../01-business/roadmap/open-decisions.md#c81))
The open questions in the status review (PC-1 to PC-6) were not answered; "complete this feature completely" was taken as approval, and the recommended answers were applied. The product owner can change any of them.

| Question | Applied answer | Where |
|---|---|---|
| PC-1 Where are files stored? | Private AWS S3 with presigned URLs, in the same bucket as the knowledge media, under `product-content/{applicationId}/…`. A CDN (CloudFront) is not built. | `ProductContentService`, `MediaStorageService` |
| PC-2 How is documentation published? | Knowledge-base articles linked to the application; the external link stays. | REQ-CAT-004.3 |
| PC-3 How are videos added? | A YouTube, Vimeo or other web link; no video upload. | REQ-CAT-004.6 |
| PC-4 What is a case study? | Customer name, logo, problem, result, optional PDF. | REQ-CAT-004.7 |
| PC-5 How much version history? | Latest version only, with the version number and an "Updated on" date. | REQ-CAT-004.4 |
| PC-6 Types and sizes? | Images PNG, JPG, WebP up to 5 MB (SVG left out, see REQ-CAT-004.5); PDF up to 20 MB. Limits are configurable (`eis.product-content.*`). | `ProductContentRules` |

## Not specified (not built, not invented)
- Moving the uploaded **logo** (`/products/images`, local disk) to S3, and a CDN in front of the bucket.
- Per-region or per-language content (belongs to 02.05 Localization).
- Version history (older versions), approval workflow or scheduled publishing for product content.
- Content for platforms (products in the UI wording); only applications have content.
- A limit on the number of items per application.
- Removal of upload objects that were never attached to an item (no cleanup job; administrators only, small files).

## Out of scope
- Offerings and bundles (02.02), localization (02.05), tax and prices.
- Video hosting; the knowledge-base video library (REQ-KNW-004) is separate.

## Dependencies
- [REQ-KNW-003](../knowledge-media/requirement.md) storage (`MediaStorageService`, private S3 bucket, `eis.knowledge.storage.*`) and [aws-s3.md](../../../09-integrations/aws-s3.md).
- [REQ-KNW](../knowledge-content/requirement.md) articles for documentation links.
- [REQ-CAT-003](../catalog-showcase/requirement.md) product page and form.
