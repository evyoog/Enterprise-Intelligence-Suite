# Knowledge Management — analytics

**Status:** Draft (2026-10-05) — not built. Requirement: REQ-KNW-006.

| Field | Value |
|---|---|
| Route | `/admin/knowledge/analytics` |
| Roles | Contributor, publisher |

## Content
| Area | Content |
|---|---|
| Period | 7 / 30 / 90 days |
| KPIs | Article views, video views, downloads, searches (successful / no result), helpful %, tickets from knowledge |
| Lists | Popular topics, content, downloads, videos; most searched; lowest rated |
| Video analytics | Views, watch completion, average watch time (where available), most watched, by source type |
| Knowledge gaps | As dashboard, with Create content |

## Common rules
- EIS shell (global header and sidebar) unchanged; EIS theme (C45, Inter, accent palette); layout and section order follow the visual direction canvas (claude.ai/artifact/G2rBj5KRcuyyDHeLTjfXas).
- Content shown only if the reader may see it ([BR-KVS-001](../../03-business-rules/BR-KVS-001-knowledge-visibility.md)).
- States: skeleton loading, empty (EmptyState with a next step), error (ErrorState with Retry).
- Motion: subtle hover, transitions and skeletons; none with *Reduce motion* or `prefers-reduced-motion`.
- Accessibility: keyboard, visible focus, labels, 4.5:1 contrast, semantic controls; axe test.
- Responsive: desktop primary; tablet collapses secondary controls; mobile stacks cards.
- i18n: `knowledge.*` in `en.json` and `es.json`.
