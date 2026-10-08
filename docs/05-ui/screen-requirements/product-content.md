# Screen — Product content (admin Content tab and public Resources tab)

**Status:** Built 2026-10-07 ([C81](../../01-business/roadmap/open-decisions.md#c81)). FRD: [REQ-CAT-004 ui-requirements](../../02-requirements/FRD/product-content/ui-requirements.md).

## Admin: Edit application → Content tab
Route `/admin/products/:id/edit?tab=content` (`MANAGE_CATALOG`). The edit screen has two tabs: **Details** (the existing form: basic information, appearance, launch and availability, integration, product relationships, features, pricing, resources, with the live Showcase Preview) and **Content**. The tab is in the URL, so a refresh or a link keeps it. The Applications list has a **Content** action on every row (folder icon), and the Add application dialog says that content is added after saving.

Content tab: an intro, a notice when file storage is not configured, then five sections. Each section has a heading, one line of help (with the size limit), an **Add** button and its items. An item row shows an optional thumbnail (image, YouTube video, case-study logo), the title, a Draft or Published chip, "Version n · updated {date}", the file name and size, and the actions **Move up**, **Move down**, **Publish / Unpublish**, **Edit** and **Delete** (all with names that include the item title). Delete asks for confirmation.

| Section | Add dialog fields |
|---|---|
| Datasheet | Title, description, PDF (up to 20 MB) |
| Documentation | Title, description, knowledge article (live, public; this application's first) |
| Images | Title, description, alt text (required), PNG/JPG/WebP (up to 5 MB) |
| Videos | Title, description, video link (https; YouTube and Vimeo recognised) |
| Case studies | Customer name (required), title, problem, result, optional customer logo (image), optional PDF |

A file is uploaded as soon as it is chosen (progress, speed, remaining size and Cancel, the same card as the knowledge media library) and attached when the dialog is saved. A wrong type or an oversized file is refused before anything is uploaded.

## Public: product page → Resources tab
Route `/products/:id`. The **Resources** tab appears between Pricing and Reviews only when the application has published content. Sections: Datasheets (Download button naming the type and size; a fresh 5-minute link is requested on click), Documentation (links to the knowledge articles), Gallery (images with their alt text and captions; click to enlarge), Videos (YouTube and Vimeo load their privacy-friendly player only after the visitor presses play; any other link opens in a new tab), Case studies (customer, logo, problem, result, optional PDF). Each item shows "Version n · updated {date}" where it applies.

## Accessibility
Every control has a visible label or an accessible name; the sections are labelled regions; reorder, publish, edit and delete are buttons; images use their alt text; iframes have titles. Tests: `ProductContent.test.tsx` (jest-axe), `EditProductPage.test.tsx`, `ProductDetailPage.test.tsx`.
