# Knowledge Center — workflow guides

**Status:** Built 2026-10-05 ([C78](../../01-business/roadmap/open-decisions.md#c78)) — `/knowledge/workflows`. Requirement: REQ-KNW-005.14.

| Field | Value |
|---|---|
| Route | `/knowledge/workflows`, `/knowledge/workflows/{slug}` |
| Roles | Readers |

## Content
| Area | Content |
|---|---|
| List | Cards: Procure-to-pay, Lead-to-cash, Production (seed; data-driven), with product and step count |
| Flow view | Horizontal step diagram (wraps on mobile to a vertical list): Purchase request → RFQ → Vendor quotation → Comparison → Purchase order → Goods receipt → Inspection → Invoice → Payment (example). Each step is a button; selecting it opens its guide in a side panel with "Open in EIS" |
| Keyboard | Arrow keys move between steps; Enter opens |

## Common rules
- EIS shell (global header and sidebar) unchanged; EIS theme (C45, Inter, accent palette); layout and section order follow the visual direction canvas (claude.ai/artifact/G2rBj5KRcuyyDHeLTjfXas).
- Content shown only if the reader may see it ([BR-KVS-001](../../03-business-rules/BR-KVS-001-knowledge-visibility.md)).
- States: skeleton loading, empty (EmptyState with a next step), error (ErrorState with Retry).
- Motion: subtle hover, transitions and skeletons; none with *Reduce motion* or `prefers-reduced-motion`.
- Accessibility: keyboard, visible focus, labels, 4.5:1 contrast, semantic controls; axe test.
- Responsive: desktop primary; tablet collapses secondary controls; mobile stacks cards.
- i18n: `knowledge.*` in `en.json` and `es.json`.
