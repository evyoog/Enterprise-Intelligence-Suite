# Knowledge Center — search results

**Status:** Built 2026-10-05 ([C78](../../01-business/roadmap/open-decisions.md#c78)) — `/knowledge/search`. Requirement: REQ-KNW-005.10, REQ-PRT-002/003.

| Field | Value |
|---|---|
| Route | `/knowledge/search?q=&type=` |
| Roles | Readers |

## Content
| Area | Content |
|---|---|
| Search box | As header; filters chips: All, Articles & guides, Videos, FAQs, Troubleshooting & error codes, Documents & templates, Workflows, Products & modules, Release notes, Developer |
| Results | Grouped by type with counts; each row: icon, title (highlighted), match label (C70), snippet (transcript passage with timestamp for videos), product › module, updated |
| Video result | Thumbnail, duration, "at 02:15" link that opens the player at the chapter/cue |
| Did you mean / no results | As C70: correction link; help tips; "Create support ticket" prefilled with the query |
| Personalisation | Results for products the reader has access to are boosted (Should) |

## Common rules
- EIS shell (global header and sidebar) unchanged; EIS theme (C45, Inter, accent palette); layout and section order follow the visual direction canvas (claude.ai/artifact/G2rBj5KRcuyyDHeLTjfXas).
- Content shown only if the reader may see it ([BR-KVS-001](../../03-business-rules/BR-KVS-001-knowledge-visibility.md)).
- States: skeleton loading, empty (EmptyState with a next step), error (ErrorState with Retry).
- Motion: subtle hover, transitions and skeletons; none with *Reduce motion* or `prefers-reduced-motion`.
- Accessibility: keyboard, visible focus, labels, 4.5:1 contrast, semantic controls; axe test.
- Responsive: desktop primary; tablet collapses secondary controls; mobile stacks cards.
- i18n: `knowledge.*` in `en.json` and `es.json`.
