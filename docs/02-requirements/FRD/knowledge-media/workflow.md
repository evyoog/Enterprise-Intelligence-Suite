# Workflow — Knowledge media

Upload flow (same as videos): [`docs/04-workflows/video-upload.md`](../../../04-workflows/video-upload.md). Download: [`video-playback.md`](../../../04-workflows/video-playback.md).

```mermaid
stateDiagram-v2
  [*] --> Pending: upload URL issued
  Pending --> Ready: complete-upload verified
  Pending --> Failed: verification failed
  Pending --> Abandoned: never completed (cleanup job deletes object)
  Ready --> Ready: replaced (new version)
  Ready --> Deleted: deleted (object removed)
```
