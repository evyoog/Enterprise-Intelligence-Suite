# Knowledge Center — video player

**Status:** Draft (2026-10-05) — not built. Requirement: REQ-KNW-004.6, REQ-KNW-005.23.

| Field | Value |
|---|---|
| Route | `/knowledge/content/{slug}` (type VIDEO) and inline video blocks |
| Roles | Readers |

## Content
| Area | Content |
|---|---|
| Player | YouTube: IFrame player; AWS S3: HTML5 `<video>` with a temporary URL (re-requested when it expires); External: configured player or "Open video" link. Native accessible controls; captions/subtitles tracks when available |
| Source badge | YouTube / Hosted / External |
| Chapters | List "00:00 Introduction" …; click seeks (YouTube and HTML5); current chapter highlighted |
| Transcript | Panel with search box; follows playback (current cue highlighted, auto-scroll that the reader can pause); click a line to seek |
| Details | Title, product, module, difficulty, duration, views, description, related content |
| Progress | Position saved (signed in) for "Continue where you left off"; analytics progress events |
| Errors | Playback URL refused → "This video isn't available to you."; network → Retry |

## Common rules
- EIS shell (global header and sidebar) unchanged; EIS theme (C45, Inter, accent palette); layout and section order follow the visual direction canvas (claude.ai/artifact/G2rBj5KRcuyyDHeLTjfXas).
- Content shown only if the reader may see it ([BR-KVS-001](../../03-business-rules/BR-KVS-001-knowledge-visibility.md)).
- States: skeleton loading, empty (EmptyState with a next step), error (ErrorState with Retry).
- Motion: subtle hover, transitions and skeletons; none with *Reduce motion* or `prefers-reduced-motion`.
- Accessibility: keyboard, visible focus, labels, 4.5:1 contrast, semantic controls; axe test.
- Responsive: desktop primary; tablet collapses secondary controls; mobile stacks cards.
- i18n: `knowledge.*` in `en.json` and `es.json`.
