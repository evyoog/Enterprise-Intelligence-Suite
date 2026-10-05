# Workflow — Secure video playback and file download

**Status:** Built 2026-10-05 ([C78](../01-business/roadmap/open-decisions.md#c78)); defined 2026-10-05). [REQ-KNW-004.6](../02-requirements/FRD/knowledge-videos/requirement.md), [REQ-KNW-003.8](../02-requirements/FRD/knowledge-media/requirement.md), [BR-KVS-001](../03-business-rules/BR-KVS-001-knowledge-visibility.md).

```mermaid
sequenceDiagram
  actor R as Reader (browser)
  participant B as EIS backend
  participant S as S3
  R->>B: GET /knowledge/videos/{id}/play-url (or /media/{id}/download-url)
  B->>B: authenticate (or Public), authorise: audience, organization, product access, published
  alt not allowed
    B-->>R: 404 (never confirms the item exists)
  else allowed
    B->>B: presign GET (expiry from configuration), record VIDEO_PLAYED / DOWNLOADED
    B-->>R: {url, expiresAt}
    R->>S: GET (HTML5 player / download)
    S-->>R: bytes (range requests)
  end
  Note over R,B: When the URL expires during playback the player asks for a new one
```
YouTube: the page embeds the YouTube player after the same audience check; External: the configured player or link.
