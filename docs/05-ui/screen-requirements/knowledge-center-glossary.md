# Knowledge Center — glossary

**Status:** Built 2026-10-05 ([C78](../../01-business/roadmap/open-decisions.md#c78)) — `/knowledge/glossary`. Requirement: REQ-KNW-005.21.

| Field | Value |
|---|---|
| Route | `/knowledge/glossary` |
| Roles | Readers |

## Content
| Area | Content |
|---|---|
| A–Z index and search | Seed: BOM, MRP, OEE, WIP, S&OP, MES, QMS, CAPA, RFQ, MOQ, EOQ |
| Term | Term, definition, synonyms, related content; terms are linked from article text (popover) |

## Common rules
- EIS shell (global header and sidebar) unchanged; EIS theme (C45, Inter, accent palette); layout and section order follow the visual direction canvas (claude.ai/artifact/G2rBj5KRcuyyDHeLTjfXas).
- Content shown only if the reader may see it ([BR-KVS-001](../../03-business-rules/BR-KVS-001-knowledge-visibility.md)).
- States: skeleton loading, empty (EmptyState with a next step), error (ErrorState with Retry).
- Motion: subtle hover, transitions and skeletons; none with *Reduce motion* or `prefers-reduced-motion`.
- Accessibility: keyboard, visible focus, labels, 4.5:1 contrast, semantic controls; axe test.
- Responsive: desktop primary; tablet collapses secondary controls; mobile stacks cards.
- i18n: `knowledge.*` in `en.json` and `es.json`.
