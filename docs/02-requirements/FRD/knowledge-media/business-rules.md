# Business rules — Knowledge media

| ID | Rule | Enforced in | Source |
|----|------|-------------|--------|
| BR-KMED-001 | All of [BR-MED-001](../../../03-business-rules/BR-MED-001-private-media-storage.md). | backend, infrastructure | C72 |
| BR-KMED-002 | An upload URL is issued only to a contributor or publisher, for an allowed type within the configured size, for an existing product and module. | backend | REQ-KNW-003.2 |
| BR-KMED-003 | A media item is usable only after `completeUpload` verified the object (exists, size equals the declared size, content type matches); until then it is PENDING and not shown. | backend | REQ-KNW-003.2 |
| BR-KMED-004 | A presigned URL is valid for one object and one method, for the configured expiry only; an expired URL fails at S3. | backend, S3 | REQ-KNW-003.2, .8 |
| BR-KMED-005 | Deleting a file in use needs explicit confirmation listing the content using it; the content's block then shows "File removed". | backend, frontend | REQ-KNW-003.6 |
| BR-KMED-006 | Every upload, failed upload, replace and delete is audited. | backend | REQ-KNW-003 |
