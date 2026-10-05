# Integration — YouTube

**Status:** Draft (2026-10-05, [C73](../01-business/roadmap/open-decisions.md#c73)). Used by [REQ-KNW-004](../02-requirements/FRD/knowledge-videos/requirement.md).

## Fetch video details
1. Parse the video id from `youtube.com/watch?v=`, `youtu.be/`, `youtube.com/embed/`, `youtube.com/shorts/` URLs (11 characters `[A-Za-z0-9_-]`); anything else → "This is not a YouTube video link."
2. **With an API key** (`EIS_YOUTUBE_API_KEY` in `config/secrets.env`, optional): `GET https://www.googleapis.com/youtube/v3/videos?part=snippet,contentDetails&id={id}` → title, description, channel, thumbnails, duration (ISO 8601 → seconds). Called from the backend only; the key never reaches the browser.
3. **Without a key (fallback):** `GET https://www.youtube.com/oembed?url={url}&format=json` → title, author (channel), thumbnail. Duration and description are typed by the admin.
4. Private, deleted or embed-disabled videos → "This video can't be embedded." Network errors → friendly message; the admin may still save the URL and type details.

## Playback
The YouTube IFrame Player (privacy-enhanced `www.youtube-nocookie.com/embed/{id}`, proposed) with the IFrame API for progress and seeking (chapters). Audience rules (BR-KVS-001) decide whether the page shows the video; YouTube itself decides public/unlisted access — **unlisted YouTube videos are not secret**: content that must stay private should use AWS S3.

## Network
Backend egress to `www.googleapis.com` (with a key) and `www.youtube.com` (oEmbed); browser loads the player from YouTube. The frontend CSP must allow the YouTube frame source.

## Configuration
| Secret / setting | Where | Meaning |
|---|---|---|
| `EIS_YOUTUBE_API_KEY` | `config/secrets.env` (empty in the template) | Optional Data API key |
| `eis.knowledge.youtube.timeout` | `application.yml` (`eis.knowledge.*`) | HTTP timeout, proposed 5 s |
