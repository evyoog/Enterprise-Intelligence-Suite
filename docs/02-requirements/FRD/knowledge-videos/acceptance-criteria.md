# Acceptance criteria — Knowledge videos

| ID | Criterion | Test cases |
|----|-----------|------------|
| AC-1 | **Given** a YouTube URL and no API key **When** Fetch video details is clicked **Then** title, thumbnail and channel are filled from oEmbed and duration/description are editable | [TC-KNW-032](../../../../test-cases/functional/knowledge-videos/TC-KNW-032.md) |
| AC-2 | **Given** an API key **When** details are fetched **Then** id, thumbnail, title, description, duration and channel are filled | [TC-KNW-033](../../../../test-cases/functional/knowledge-videos/TC-KNW-033.md) |
| AC-3 | **Given** an MP4 within the size limit **When** a contributor uploads it **Then** progress, speed and Cancel show, the browser uploads to S3 directly, and the video is saved after verification | [TC-KNW-034](../../../../test-cases/functional/knowledge-videos/TC-KNW-034.md) |
| AC-4 | **Given** a MOV over the limit or an AVI **When** chosen **Then** a friendly error is shown and nothing is uploaded | [TC-KNW-035](../../../../test-cases/functional/knowledge-videos/TC-KNW-035.md) |
| AC-5 | **Given** a published S3 video restricted to organization A **When** a member of B requests its play URL **Then** 404; a member of A gets a temporary URL that plays and later expires | [TC-KNW-036](../../../../test-cases/functional/knowledge-videos/TC-KNW-036.md) |
| AC-6 | **Given** a video with transcript and chapters **When** a reader plays it **Then** the transcript follows playback, is searchable, and clicking a chapter seeks | [TC-KNW-037](../../../../test-cases/functional/knowledge-videos/TC-KNW-037.md) |
| AC-7 | **Given** a search for a phrase spoken only in a transcript **When** searched **Then** the video is found | [TC-KNW-038](../../../../test-cases/functional/knowledge-videos/TC-KNW-038.md) |
| AC-8 | **Given** a publisher deletes an S3 video **When** confirmed **Then** the object is deleted, the record and search entry removed, and the action audited | [TC-KNW-039](../../../../test-cases/functional/knowledge-videos/TC-KNW-039.md) |
