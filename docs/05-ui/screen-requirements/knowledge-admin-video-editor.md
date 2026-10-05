# Knowledge Management — add / edit video

**Status:** Draft (2026-10-05) — not built. Requirement: REQ-KNW-004.3–.5, .8–.11.

| Field | Value |
|---|---|
| Route | `/admin/knowledge/videos/new`, `/admin/knowledge/videos/{id}` |
| Roles | Contributor, publisher |

## Content
| Area | Content |
|---|---|
| Source choice | YouTube URL / AWS S3 upload / External URL (radio cards) |
| YouTube | URL field, Fetch video details → fills id, thumbnail, title, description, duration, channel (Data API) or title, thumbnail, channel (oEmbed; duration and description editable) |
| AWS S3 | Drag-and-drop area or Choose video; MP4, WebM, MOV; maximum size shown from configuration; upload progress component |
| External | URL and metadata |
| Metadata | Title*, product*, module*, category*, description, tags, audience, thumbnail (Upload; Generate from video = disabled "Not available yet"), transcript (Upload, Paste; Auto-generate disabled), chapters editor, subtitles (VTT per language) |
| Storage information (S3, publisher) | Provider, bucket, region, object path, size, format, upload status |
| Delete | Confirm "Delete video? This will remove the video from the Knowledge Center and delete its associated storage object." |
| Replace | Upload a new file → verify → new version |

## Common rules
- EIS shell (global header and sidebar) unchanged; EIS theme (C45, Inter, accent palette); layout and section order follow the visual direction canvas (claude.ai/artifact/G2rBj5KRcuyyDHeLTjfXas).
- Content shown only if the reader may see it ([BR-KVS-001](../../03-business-rules/BR-KVS-001-knowledge-visibility.md)).
- States: skeleton loading, empty (EmptyState with a next step), error (ErrorState with Retry).
- Motion: subtle hover, transitions and skeletons; none with *Reduce motion* or `prefers-reduced-motion`.
- Accessibility: keyboard, visible focus, labels, 4.5:1 contrast, semantic controls; axe test.
- Responsive: desktop primary; tablet collapses secondary controls; mobile stacks cards.
- i18n: `knowledge.*` in `en.json` and `es.json`.
