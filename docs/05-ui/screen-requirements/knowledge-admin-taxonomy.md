# Knowledge Management — taxonomy

**Status:** Draft (2026-10-05) — not built. Requirement: REQ-KNW-002.4, C74.

| Field | Value |
|---|---|
| Route | `/admin/knowledge/taxonomy` |
| Roles | Publisher (contributor read-only) |

## Content
| Area | Content |
|---|---|
| Products | Linked catalog products, order, active |
| Modules | Per product (seed lists), add, rename, reorder, deactivate (delete refused when in use) |
| Categories | Including troubleshooting categories and video kinds |

## Common rules
- EIS shell (global header and sidebar) unchanged; EIS theme (C45, Inter, accent palette); layout and section order follow the visual direction canvas (claude.ai/artifact/G2rBj5KRcuyyDHeLTjfXas).
- Content shown only if the reader may see it ([BR-KVS-001](../../03-business-rules/BR-KVS-001-knowledge-visibility.md)).
- States: skeleton loading, empty (EmptyState with a next step), error (ErrorState with Retry).
- Motion: subtle hover, transitions and skeletons; none with *Reduce motion* or `prefers-reduced-motion`.
- Accessibility: keyboard, visible focus, labels, 4.5:1 contrast, semantic controls; axe test.
- Responsive: desktop primary; tablet collapses secondary controls; mobile stacks cards.
- i18n: `knowledge.*` in `en.json` and `es.json`.
