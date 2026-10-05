# UAT-KNW-002 — Media library

Needs the S3 test bucket (see README); without it the library says storage is not configured.

| Step | Who | Action | Expected |
|---|---|---|---|
| 1 | Karthik | Media library → Upload a PDF manual. | Progress shown; the file appears with an id; the browser's network panel shows the PUT going to S3, not to EIS. |
| 2 | Karthik | Upload an `.exe`, and a PDF larger than 100 MB. | Friendly "not allowed" / "too large" messages; nothing uploaded. |
| 3 | Karthik | Add a PDF block with the file id to an article; Priya publishes it for organization "UAT Org" only. | Asha can download it (the link expires after 5 minutes); Ravi and signed-out visitors cannot see the article or the file. |
| 4 | Priya | Open the file's storage information. | Provider, bucket, region, object path, size, format, status — no keys. |
| 5 | Priya | Delete the file. | A warning lists the article; after confirming, the article shows "File removed"; audit log entry. |
