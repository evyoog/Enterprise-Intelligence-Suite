# Workflow — Knowledge videos

Upload: [`video-upload.md`](../../../04-workflows/video-upload.md). Playback: [`video-playback.md`](../../../04-workflows/video-playback.md). Publishing states: [`content-publishing.md`](../../../04-workflows/content-publishing.md).

```mermaid
flowchart TD
  A[+ Add video] --> S{Source}
  S -->|YouTube| Y[Paste URL → Fetch details: Data API or oEmbed]
  S -->|AWS S3| U[Choose file → upload URL → direct upload → verify]
  S -->|External URL| E[URL + metadata]
  Y --> M[Metadata, audience, thumbnail, transcript, chapters, subtitles]
  U --> M
  E --> M
  M --> D[Draft] --> R[Review → Publish (REQ-KNW-002)]
```
