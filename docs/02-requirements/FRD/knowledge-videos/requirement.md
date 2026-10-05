# REQ-KNW-004 — Knowledge video management

**Status:** Draft — waits for "Approved"
**Owner:** Product owner
**Decisions:** [C72](../../../01-business/roadmap/open-decisions.md#c72), [C73](../../../01-business/roadmap/open-decisions.md#c73)

| Field | Value |
|---|---|
| Sprint | [2027.1.1](../../../01-business/roadmap/sprints/SPRINT-2027.1.1.md) (videos in the knowledge base); video learning 11.03.02 Stream video / Track watch progress belongs to [2027.1.3](../../../01-business/roadmap/sprints/SPRINT-2027.1.3.md) — dates unchanged |
| Requirement ID | REQ-KNW-004 |
| Application | [11 Training & Knowledge Management](../../../01-business/roadmap/applications/11-training-knowledge-management.md) |
| Priority | P0 (11.03.02 is P1) |
| Integrations | [aws-s3.md](../../../09-integrations/aws-s3.md), [youtube.md](../../../09-integrations/youtube.md) |

## Summary
Videos are first-class knowledge content (type VIDEO) from three sources — YouTube, AWS S3 upload, external URL — behind a `VideoSourceProvider` interface. Publishers and contributors manage metadata, thumbnails, transcripts, chapters and subtitles; readers watch through the right player with a searchable transcript and clickable chapters; S3 playback uses short-lived presigned URLs after an authorization check.

## Functional requirements
| ID | Requirement | Priority |
|---|---|---|
| REQ-KNW-004.1 | **Sources:** `YOUTUBE`, `AWS_S3`, `EXTERNAL_URL`, each a `VideoSourceProvider` (`YouTubeVideoService`, `S3VideoService` on `MediaStorageService`, `ExternalVideoService`). | Must |
| REQ-KNW-004.2 | **Video screen:** summary cards (total, published, draft, under review, total views); search by title, description, tags; filters product, module, category, status, source; table (checkbox, thumbnail, title, description, category, tags, product, module, source badge, duration, status, views, updated, actions); cards on mobile. | Must |
| REQ-KNW-004.3 | **Add video:** choose the source first. YouTube: paste URL → **Fetch video details** (Data API with a key: id, thumbnail, title, description, duration, channel; without a key: oEmbed for title, thumbnail, channel; duration and description typed). External: URL and metadata by hand. AWS S3: drag and drop or Choose video; MP4, WebM, MOV; maximum size from configuration. | Must |
| REQ-KNW-004.4 | **Metadata for every source:** title*, product*, module*, category*, description, tags, audience (BR-KVS-001), thumbnail (Upload; *Generate from video* = integration point), transcript (Upload VTT/SRT/TXT, Paste; *Auto-generate* = integration point), chapters ("00:00 Introduction" lines), subtitles (VTT per language). | Must |
| REQ-KNW-004.5 | **S3 upload** follows the mandatory flow (metadata → checks → presigned PUT with unique key → direct upload with progress, speed, remaining, Cancel → completion → HEAD verification → record). States: preparing, uploading, completed, failed, cancelled; the UI never freezes; multipart above the threshold (REQ-KNW-003.3). | Must |
| REQ-KNW-004.6 | **Playback:** YouTube embed player (privacy-enhanced domain, proposed); S3: HTML5 player with a temporary presigned GET URL from `GET /knowledge/videos/{id}/play-url` after authorization; External: the configured player or a link. Transcript follows playback and is searchable; chapters are clickable where the player supports seeking. | Must |
| REQ-KNW-004.7 | **Analytics:** views; watch progress and completion where the player reports it (HTML5 and YouTube IFrame API; external: views only). | Must |
| REQ-KNW-004.8 | **Delete:** confirmation "Delete video? This will remove the video from the Knowledge Center and delete its associated storage object." → authorise → delete the S3 object (S3 source) → update the record and search index → audit. | Must |
| REQ-KNW-004.9 | **Replace:** upload the new file → verify → new version of the video item → old object kept or deleted per retention (REQ-KNW-003 open question 2). | Must |
| REQ-KNW-004.10 | **Search:** title, description, tags, product, module, category, transcript and chapters are indexed (C76); transcript passages are embedded for semantic search when the video is public. | Must |
| REQ-KNW-004.11 | **Storage information** for S3 videos (publishers): provider, bucket, region, object path, size, format, upload status — no credentials. | Must |
| REQ-KNW-004.12 | **Audit:** video uploaded, upload failed, published, unpublished, replaced, deleted. | Must |
| REQ-KNW-004.13 | **Errors** as REQ-KNW-003.11. | Must |

## Data stored (metadata only, never video binary)
`id, title, description, video_source_type, video_url, video_id, s3_bucket, s3_object_key, thumbnail_url (or thumbnail media id), file_name, file_size, mime_type, duration, product_id, module_id, category_id, tags, transcript, chapters, visibility, status, version, created_by, created_at, updated_by, updated_at` — see [data model](../../../07-database/data-model/knowledge.md). S3 fields only for AWS_S3.

## Out of scope
- Transcoding, adaptive streaming (HLS/DASH), automatic transcripts and thumbnails — integration points only (open question).
- Existing YouTube videos: none exist in the repository (inventory §11), so nothing needs migrating.

## Open questions
| # | Question | Blocks approval |
|---|---|---|
| 1 | YouTube Data API key: provided, or oEmbed only? | No — oEmbed fallback works without it |
| 2 | Maximum video size and playback URL expiry. | Yes (same as REQ-KNW-003 Q1) |
| 3 | Automatic transcripts and thumbnails later: which service? | No |
| 4 | Thumbnail for S3 videos when none is uploaded: generic placeholder (proposed)? | No — confirm in review |
| 5 | Which external players are supported for EXTERNAL_URL (Vimeo, Wistia, plain MP4 link)? Not specified. | No — confirm in review |
