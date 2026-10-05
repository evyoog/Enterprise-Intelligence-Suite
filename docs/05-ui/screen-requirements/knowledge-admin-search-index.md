# Knowledge Management — search index

**Status:** Draft (2026-10-05) — not built. Requirement: REQ-KNW-005.10, C76.

| Field | Value |
|---|---|
| Route | `/admin/knowledge/search-index` |
| Roles | Publisher |

## Content
| Area | Content |
|---|---|
| Counts | Indexed documents, videos, transcripts, PDFs (and other knowledge types) |
| Embeddings | Embedding status from REQ-PRT-003 (passages embedded / pending, model) |
| Last indexing | Time and result |
| Failed items | Item, reason, Re-index |
| Actions | Re-index all knowledge, Re-index one item; link to `/admin/search` for the full platform rebuild (`MANAGE_SEARCH`) |

## Common rules
- EIS shell (global header and sidebar) unchanged; EIS theme (C45, Inter, accent palette); layout and section order follow the visual direction canvas (claude.ai/artifact/G2rBj5KRcuyyDHeLTjfXas).
- Content shown only if the reader may see it ([BR-KVS-001](../../03-business-rules/BR-KVS-001-knowledge-visibility.md)).
- States: skeleton loading, empty (EmptyState with a next step), error (ErrorState with Retry).
- Motion: subtle hover, transitions and skeletons; none with *Reduce motion* or `prefers-reduced-motion`.
- Accessibility: keyboard, visible focus, labels, 4.5:1 contrast, semantic controls; axe test.
- Responsive: desktop primary; tablet collapses secondary controls; mobile stacks cards.
- i18n: `knowledge.*` in `en.json` and `es.json`.
