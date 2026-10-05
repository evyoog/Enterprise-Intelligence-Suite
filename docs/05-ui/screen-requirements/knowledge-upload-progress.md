# Knowledge Management — upload progress (component)

**Status:** Draft (2026-10-05) — not built. Requirement: REQ-KNW-003.2–.3, REQ-KNW-004.5.

| Field | Value |
|---|---|
| Route | Used by media library, video editor and block editor |
| Roles | Contributor, publisher |

## Content
| Element | Behaviour |
|---|---|
| Row per file | File name, size, uploaded amount, percentage, progress bar (`aria-valuenow`), speed and remaining where available, Cancel |
| States | Preparing → Uploading → Completed / Failed (reason, Retry) / Cancelled |
| Non-blocking | The rest of the page stays usable; leaving the page during upload asks for confirmation |
| Errors | Invalid format, too large, network failure, upload URL expired (Retry gets a new URL), permission denied, storage unavailable, duplicate upload |

## Common rules
- EIS shell (global header and sidebar) unchanged; EIS theme (C45, Inter, accent palette); layout and section order follow the visual direction canvas (claude.ai/artifact/G2rBj5KRcuyyDHeLTjfXas).
- Content shown only if the reader may see it ([BR-KVS-001](../../03-business-rules/BR-KVS-001-knowledge-visibility.md)).
- States: skeleton loading, empty (EmptyState with a next step), error (ErrorState with Retry).
- Motion: subtle hover, transitions and skeletons; none with *Reduce motion* or `prefers-reduced-motion`.
- Accessibility: keyboard, visible focus, labels, 4.5:1 contrast, semantic controls; axe test.
- Responsive: desktop primary; tablet collapses secondary controls; mobile stacks cards.
- i18n: `knowledge.*` in `en.json` and `es.json`.
