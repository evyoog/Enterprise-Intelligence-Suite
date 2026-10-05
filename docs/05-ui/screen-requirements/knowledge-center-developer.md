# Knowledge Center — Developer Center

**Status:** Draft (2026-10-05) — not built. Requirement: REQ-KNW-005.20.

| Field | Value |
|---|---|
| Route | `/knowledge/developers` |
| Roles | Readers with the Developer audience where set |

## Content
| Area | Content |
|---|---|
| Navigation | API documentation, Authentication, API keys (link to Account → Security → API keys, REQ-INT-001), REST APIs, Webhooks, Events (REQ-INT-002), SDKs, Code examples, Request & response, Errors, Rate limits, Integration guides |
| Code blocks | Monospace, language label, Copy button with "Copied" confirmation |

## Common rules
- EIS shell (global header and sidebar) unchanged; EIS theme (C45, Inter, accent palette); layout and section order follow the visual direction canvas (claude.ai/artifact/G2rBj5KRcuyyDHeLTjfXas).
- Content shown only if the reader may see it ([BR-KVS-001](../../03-business-rules/BR-KVS-001-knowledge-visibility.md)).
- States: skeleton loading, empty (EmptyState with a next step), error (ErrorState with Retry).
- Motion: subtle hover, transitions and skeletons; none with *Reduce motion* or `prefers-reduced-motion`.
- Accessibility: keyboard, visible focus, labels, 4.5:1 contrast, semantic controls; axe test.
- Responsive: desktop primary; tablet collapses secondary controls; mobile stacks cards.
- i18n: `knowledge.*` in `en.json` and `es.json`.
