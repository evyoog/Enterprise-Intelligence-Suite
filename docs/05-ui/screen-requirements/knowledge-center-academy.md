# Knowledge Center — Academy (only if confirmed)

**Status:** Draft (2026-10-05) — not built. Requirement: REQ-KNW-005.22 (11b, sprint 2027.1.3, P1 stretch).

| Field | Value |
|---|---|
| Route | `/knowledge/academy` |
| Roles | Signed-in readers |

## Content
| Area | Content |
|---|---|
| Courses | Cards: title (for example "Valam.ai Fundamentals"), product, lessons, duration, progress |
| Course | Lessons (videos, guides), quizzes/assessments, progress, completion, certificate |
Hidden until the product owner confirms the Academy.

## Common rules
- EIS shell (global header and sidebar) unchanged; EIS theme (C45, Inter, accent palette); layout and section order follow the visual direction canvas (claude.ai/artifact/G2rBj5KRcuyyDHeLTjfXas).
- Content shown only if the reader may see it ([BR-KVS-001](../../03-business-rules/BR-KVS-001-knowledge-visibility.md)).
- States: skeleton loading, empty (EmptyState with a next step), error (ErrorState with Retry).
- Motion: subtle hover, transitions and skeletons; none with *Reduce motion* or `prefers-reduced-motion`.
- Accessibility: keyboard, visible focus, labels, 4.5:1 contrast, semantic controls; axe test.
- Responsive: desktop primary; tablet collapses secondary controls; mobile stacks cards.
- i18n: `knowledge.*` in `en.json` and `es.json`.
