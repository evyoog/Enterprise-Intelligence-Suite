# Knowledge Management — dashboard

**Status:** Draft (2026-10-05) — not built. Requirement: REQ-KNW-006, REQ-KNW-002.9.

| Field | Value |
|---|---|
| Route | `/admin/knowledge` |
| Roles | Contributor, publisher |

## Content
| Area | Content |
|---|---|
| Counts | Total, Published, Draft, Under review, Scheduled, Expired, Requires review (past review date); by type: videos, documents, articles, FAQs, troubleshooting |
| Analytics | Most viewed, Most searched, No-result searches, Lowest rated |
| Knowledge gaps | "Knowledge gap detected — 'supplier approval configuration' — 284 searches — 0 results" + Create content |
| Navigation (left, within Knowledge Management) | Dashboard, Articles, Videos, Documents, Templates, FAQs, Troubleshooting, Release notes, Glossary, Courses (if confirmed), Workflows, Media library, Taxonomy, Search index, Analytics |

## Common rules
- EIS shell (global header and sidebar) unchanged; EIS theme (C45, Inter, accent palette); layout and section order follow the visual direction canvas (claude.ai/artifact/G2rBj5KRcuyyDHeLTjfXas).
- Content shown only if the reader may see it ([BR-KVS-001](../../03-business-rules/BR-KVS-001-knowledge-visibility.md)).
- States: skeleton loading, empty (EmptyState with a next step), error (ErrorState with Retry).
- Motion: subtle hover, transitions and skeletons; none with *Reduce motion* or `prefers-reduced-motion`.
- Accessibility: keyboard, visible focus, labels, 4.5:1 contrast, semantic controls; axe test.
- Responsive: desktop primary; tablet collapses secondary controls; mobile stacks cards.
- i18n: `knowledge.*` in `en.json` and `es.json`.
