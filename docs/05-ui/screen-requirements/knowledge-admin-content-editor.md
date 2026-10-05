# Knowledge Management — content editor

**Status:** Draft (2026-10-05) — not built. Requirement: REQ-KNW-002.1–.8.

| Field | Value |
|---|---|
| Route | `/admin/knowledge/{type}/new`, `/admin/knowledge/{type}/{id}` |
| Roles | Contributor, publisher |

## Content
| Area | Content |
|---|---|
| Header bar | Title, status chip, version, Save draft, Preview, Submit for review, Publish (publisher; with minor/major and schedule) |
| Metadata panel | Title, short description, type, product, module, feature, category, tags, keywords, audience (Public, Customer, Organization, Admin, Developer, Partner, roles/groups/products, restrict to product access), product version, documentation version, effective, review and expiry dates |
| Block editor | Block list; add block menu (heading, paragraph, bulleted/numbered list, quote, image, gallery, video, audio, file, PDF, table, code, callout, warning, note, step, accordion, FAQ, button, link, workflow diagram, embed); drag handle **and** keyboard move up/down buttons; media blocks open the media picker or upload (progress component) |
| Type fields | FAQ, troubleshooting, error code, glossary, release note, workflow steps, document/template file |
| Versions tab | List, view, compare two (side-by-side differences), restore, deprecate |
| Workflow tab | Author, reviewer, approver, dates; review comments |
| Preview | Opens the Knowledge Center rendering for a chosen audience |
| Leave guard | Unsaved changes warning |

## Common rules
- EIS shell (global header and sidebar) unchanged; EIS theme (C45, Inter, accent palette); layout and section order follow the visual direction canvas (claude.ai/artifact/G2rBj5KRcuyyDHeLTjfXas).
- Content shown only if the reader may see it ([BR-KVS-001](../../03-business-rules/BR-KVS-001-knowledge-visibility.md)).
- States: skeleton loading, empty (EmptyState with a next step), error (ErrorState with Retry).
- Motion: subtle hover, transitions and skeletons; none with *Reduce motion* or `prefers-reduced-motion`.
- Accessibility: keyboard, visible focus, labels, 4.5:1 contrast, semantic controls; axe test.
- Responsive: desktop primary; tablet collapses secondary controls; mobile stacks cards.
- i18n: `knowledge.*` in `en.json` and `es.json`.
