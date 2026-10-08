# UI requirements — Product content

## Screens
| Screen | Route | Roles | Notes |
|---|---|---|---|
| Edit application → **Content** tab | `/admin/products/:id/edit?tab=content` | `MANAGE_CATALOG` | The Details tab is the existing form (basic information, appearance, launch, integration, relationships, features, pricing, resources) with its live Showcase Preview. |
| Product page → **Resources** tab | `/products/:id` | everyone | Shown when the application has published content. |

Create application: the Content tab needs a saved application, so the create dialog tells the administrator that content is added after saving, and the list has a **Content** action for each application.

## Content tab sections
Datasheet · Documentation · Images · Videos · Case studies. Each lists its items (title, status chip, version and updated date, file name and size) with **Add**, **Edit**, **Replace file**, **Publish / Unpublish**, **Move up / down** and **Delete** (confirmation). Upload shows progress, speed and a cancel button (same card as the knowledge media library).

## Fields and validation
| Field | Type | Required | Validation | Error message (i18n key) |
|---|---|---|---|---|
| Title | text | yes | up to 200 characters | `productContent.errors.title` |
| Description | text | no | up to 1,000 characters | |
| File (datasheet, case-study PDF) | PDF | yes for a datasheet | up to 20 MB | `productContent.errors.fileType`, `.fileSize` |
| Image | PNG, JPG, WebP | yes | up to 5 MB | same |
| Alt text | text | yes for an image | up to 250 characters | `productContent.errors.alt` |
| Video link | URL | yes | `https`; YouTube/Vimeo recognised | `productContent.errors.videoUrl` |
| Customer name, problem, result | text | customer name yes | | `productContent.errors.customer` |
| Linked article | choice | yes for documentation | live, public articles | |

## States
- Empty: each section says what can be added and why.
- Loading: skeletons. Error: an alert with Retry.
- Storage not configured: the file buttons are disabled with the message "File storage is not configured yet. Links and case studies without files still work."

## Accessibility and localization
- Every control has a visible label and accessible name; reorder and delete are buttons, so everything works with the keyboard.
- Gallery images use the alt text; videos have a title; downloads say the file type and size.
- Text is under `productContent` in `en.json` and `es.json`.
