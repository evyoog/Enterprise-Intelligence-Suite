# Acceptance criteria — Knowledge media

| ID | Criterion | Test cases |
|----|-----------|------------|
| AC-1 | **Given** a contributor **When** they request an upload URL for an allowed file **Then** they get a presigned PUT URL for a new key, and the browser uploads directly to S3 | To be written with the build |
| AC-2 | **Given** a file of a disallowed type or over the size limit **When** an upload URL is requested **Then** 400 with a friendly message | To be written with the build |
| AC-3 | **Given** a completed upload whose size differs from the declared size **When** completion is reported **Then** the item is Failed and not usable | To be written with the build |
| AC-4 | **Given** a reader allowed to see an article with a PDF **When** they download it **Then** they get a temporary URL; a reader not allowed gets 403/404 | To be written with the build |
| AC-5 | **Given** a presigned URL past its expiry **When** it is used **Then** S3 refuses it | To be written with the build |
| AC-6 | **Given** a file used by two articles **When** a publisher deletes it **Then** a warning lists both; after confirming, the object is gone and the action is audited | To be written with the build |
| AC-7 | **Given** any API response or the built frontend bundle **When** searched for AWS keys or secrets **Then** none is found | To be written with the build |
| AC-8 | **Given** an upload never completed **When** the cleanup job runs after the configured time **Then** the object is deleted | To be written with the build |
