# Knowledge Center — downloads, resources and templates

**Status:** Built 2026-10-05 ([C78](../../01-business/roadmap/open-decisions.md#c78)) — `/knowledge/downloads`. Requirement: REQ-KNW-005.17–.18, REQ-KNW-003.8.

| Field | Value |
|---|---|
| Route | `/knowledge/downloads`, `/knowledge/templates` |
| Roles | Readers |

## Content
| Area | Content |
|---|---|
| Filters | Type (user manual, implementation guide, configuration guide, brochure, release notes, template, checklist, sample document, API documentation), product, file type |
| Table (cards on mobile) | Name, file type icon, size, version, product, updated, View, Download |
| Download | Requests a temporary URL; shows a spinner; friendly error if refused or expired |
| Template library | Seed: purchase order, RFQ, employee import, attendance import, chart of accounts, opening balance, BOM, routing |

## Common rules
- EIS shell (global header and sidebar) unchanged; EIS theme (C45, Inter, accent palette); layout and section order follow the visual direction canvas (claude.ai/artifact/G2rBj5KRcuyyDHeLTjfXas).
- Content shown only if the reader may see it ([BR-KVS-001](../../03-business-rules/BR-KVS-001-knowledge-visibility.md)).
- States: skeleton loading, empty (EmptyState with a next step), error (ErrorState with Retry).
- Motion: subtle hover, transitions and skeletons; none with *Reduce motion* or `prefers-reduced-motion`.
- Accessibility: keyboard, visible focus, labels, 4.5:1 contrast, semantic controls; axe test.
- Responsive: desktop primary; tablet collapses secondary controls; mobile stacks cards.
- i18n: `knowledge.*` in `en.json` and `es.json`.
