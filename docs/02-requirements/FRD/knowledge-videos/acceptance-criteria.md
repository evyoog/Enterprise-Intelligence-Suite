# Acceptance criteria — Knowledge videos

| ID | Criterion | Test cases |
|----|-----------|------------|
| AC-1 | **Given** a YouTube URL and no API key **When** Fetch video details is clicked **Then** title, thumbnail and channel are filled from oEmbed and duration/description are editable | To be written with the build |
| AC-2 | **Given** an API key **When** details are fetched **Then** id, thumbnail, title, description, duration and channel are filled | To be written with the build |
| AC-3 | **Given** an MP4 within the size limit **When** a contributor uploads it **Then** progress, speed and Cancel show, the browser uploads to S3 directly, and the video is saved after verification | To be written with the build |
| AC-4 | **Given** a MOV over the limit or an AVI **When** chosen **Then** a friendly error is shown and nothing is uploaded | To be written with the build |
| AC-5 | **Given** a published S3 video restricted to organization A **When** a member of B requests its play URL **Then** 404; a member of A gets a temporary URL that plays and later expires | To be written with the build |
| AC-6 | **Given** a video with transcript and chapters **When** a reader plays it **Then** the transcript follows playback, is searchable, and clicking a chapter seeks | To be written with the build |
| AC-7 | **Given** a search for a phrase spoken only in a transcript **When** searched **Then** the video is found | To be written with the build |
| AC-8 | **Given** a publisher deletes an S3 video **When** confirmed **Then** the object is deleted, the record and search entry removed, and the action audited | To be written with the build |
