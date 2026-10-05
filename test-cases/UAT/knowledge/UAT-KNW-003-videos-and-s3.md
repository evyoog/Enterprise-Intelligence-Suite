# UAT-KNW-003 — Videos: YouTube, hosted (S3) and external; secure playback

| Step | Who | Action | Expected |
|---|---|---|---|
| 1 | Karthik | Videos → Add video → YouTube URL; paste a link; Fetch video details. | Without an API key: title, channel and thumbnail filled, duration and description typed by hand. With `EIS_YOUTUBE_API_KEY`: all filled. |
| 2 | Karthik | Choose product, module, category; add chapters "00:00 Introduction" and "02:15 Inventory setup" and a transcript; save; submit. Priya publishes. | Readers see the video with chapters and transcript. |
| 3 | Karthik | Add video → Upload (AWS S3); drop an MP4 (< 5 GB). Cancel once, then upload again. | Progress, speed, remaining and Cancel; cancel stops cleanly; the second upload verifies and the video saves. |
| 4 | Karthik | Try an AVI file. | Refused with a friendly message. |
| 5 | Priya | Publish the hosted video for "UAT Org" only. Asha plays it; copy the video address from the browser; wait 1 hour and open it again. | Plays for Asha; the copied link stops working after it expires (S3 "Request has expired"). Ravi gets "not available". |
| 6 | Asha | Search for a phrase only spoken in the transcript. | The video is found. |
| 7 | Karthik | Add video → External URL with `http://…`. | Refused: https only. |
| 8 | Priya | Delete the hosted video. | Confirmation text about deleting the storage object; the object is gone from the bucket; audit entry. |
