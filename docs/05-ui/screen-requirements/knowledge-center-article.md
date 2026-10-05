# Knowledge Center — article (multimedia)

**Status:** Draft (2026-10-05) — not built. Requirement: REQ-KNW-005.12–.13.

| Field | Value |
|---|---|
| Route | `/knowledge/content/{slug}` |
| Roles | Readers |

## Content
| Area | Content |
|---|---|
| Breadcrumb | Knowledge Center › product › module |
| Header | Title, short description, type chip, product, version, updated date, reading time, Bookmark, Share link (copy) |
| Body | Blocks in order: rich text, images and annotated screenshots (zoom), diagrams, video (player), audio, PDF/DOC/XLS/PPT/CSV attachments (View, Download), tables, code with Copy, callouts/warnings/notes, steps, accordions, FAQ, buttons, links, workflow diagram, embeds. Glossary terms underlined with a definition popover |
| Side rail (desktop) | On this page (headings), related product, related articles, direct action ("Open in EIS" when the reader can open the page) |
| Deprecated banner | "This content is deprecated" with a link to the replacement if set |
| Feedback | "Was this helpful?" Yes / No; on No: reason radio (Information unclear, Outdated, Missing information, Couldn't solve my problem, Other) + comment; Report outdated content; Suggest improvement (dialog) |
| Support | Still need help? (prefilled ticket) |

## Common rules
- EIS shell (global header and sidebar) unchanged; EIS theme (C45, Inter, accent palette); layout and section order follow the visual direction canvas (claude.ai/artifact/G2rBj5KRcuyyDHeLTjfXas).
- Content shown only if the reader may see it ([BR-KVS-001](../../03-business-rules/BR-KVS-001-knowledge-visibility.md)).
- States: skeleton loading, empty (EmptyState with a next step), error (ErrorState with Retry).
- Motion: subtle hover, transitions and skeletons; none with *Reduce motion* or `prefers-reduced-motion`.
- Accessibility: keyboard, visible focus, labels, 4.5:1 contrast, semantic controls; axe test.
- Responsive: desktop primary; tablet collapses secondary controls; mobile stacks cards.
- i18n: `knowledge.*` in `en.json` and `es.json`.
