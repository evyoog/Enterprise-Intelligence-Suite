# Knowledge Management — videos

**Status:** Draft (2026-10-05) — not built. Requirement: REQ-KNW-004.2.

| Field | Value |
|---|---|
| Route | `/admin/knowledge/videos` |
| Roles | Contributor, publisher |

## Content
| Area | Content |
|---|---|
| Summary cards | Total videos, Published, Draft, Under review, Total views |
| Search | "Search videos by title, description, tags…" |
| Filters | Product, module, category, status, source (All, YouTube, AWS S3, External URL) |
| Table (cards on mobile) | Checkbox; thumbnail, title, description, category, tags; product › module; source badge; duration; status; views; updated on; actions (Edit, Preview, Replace, Delete by permission) |
| + Add video | Opens the video editor at source choice |

## Common rules
- EIS shell (global header and sidebar) unchanged; EIS theme (C45, Inter, accent palette); layout and section order follow the visual direction canvas (claude.ai/artifact/G2rBj5KRcuyyDHeLTjfXas).
- Content shown only if the reader may see it ([BR-KVS-001](../../03-business-rules/BR-KVS-001-knowledge-visibility.md)).
- States: skeleton loading, empty (EmptyState with a next step), error (ErrorState with Retry).
- Motion: subtle hover, transitions and skeletons; none with *Reduce motion* or `prefers-reduced-motion`.
- Accessibility: keyboard, visible focus, labels, 4.5:1 contrast, semantic controls; axe test.
- Responsive: desktop primary; tablet collapses secondary controls; mobile stacks cards.
- i18n: `knowledge.*` in `en.json` and `es.json`.
