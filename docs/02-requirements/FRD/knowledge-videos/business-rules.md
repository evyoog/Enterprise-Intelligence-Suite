# Business rules — Knowledge videos

| ID | Rule | Enforced in | Source |
|----|------|-------------|--------|
| BR-KVID-001 | `video_source_type` decides required fields: YOUTUBE → `video_id` (11 characters, parsed from watch, youtu.be, embed or shorts URLs); AWS_S3 → `s3_bucket`, `s3_object_key`, `file_size`, `mime_type`; EXTERNAL_URL → `video_url` (https only). | backend | REQ-KNW-004.1, .3 |
| BR-KVID-002 | S3 videos: MP4, WebM or MOV only; size ≤ `eis.knowledge.video.max-size`. | backend | REQ-KNW-004.3 |
| BR-KVID-003 | A play URL is issued only when the reader may see the video (BR-KVS-001); expiry `eis.knowledge.playback-url-expiry`. Another organization's restricted video always answers 404. | backend | REQ-KNW-004.6 |
| BR-KVID-004 | Chapters are "mm:ss Title" or "hh:mm:ss Title" lines, in increasing time order, within the duration when known. | backend | REQ-KNW-004.4 |
| BR-KVID-005 | Transcripts are stored as text with optional cue times (VTT/SRT parsed); they are indexed but never executed or rendered as HTML. | backend | REQ-KNW-004.4, .10 |
| BR-KVID-006 | All of BR-MED-001 and BR-KMED-002–006 apply to S3 videos. | backend | C72 |
