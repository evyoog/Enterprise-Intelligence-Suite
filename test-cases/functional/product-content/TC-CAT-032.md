# TC-CAT-032: Upload, download and playback against a real S3 bucket (manual)

| Field | Value |
|---|---|
| Test Case ID (required) | TC-CAT-032 |
| Requirement ID (required) | [REQ-CAT-004](../../../docs/02-requirements/FRD/product-content/requirement.md) — [acceptance criteria](../../../docs/02-requirements/FRD/product-content/acceptance-criteria.md) |
| Priority | P0 |
| Type | Functional |
| Automated | No |

## Preconditions
A test bucket with CORS for the web origin (PUT, GET, HEAD, expose ETag), `eis.knowledge.storage.*` set, credentials only in config/secrets.env. A platform administrator.

## Steps
1. Add a 3 MB PDF datasheet and a 1 MB PNG image; publish both.
2. As a visitor, open the product's Resources tab, click Download.
3. Wait six minutes and use the same download link.
4. Replace the datasheet's file; check the bucket.
5. Delete the application; check the bucket.

## Expected Result
The browser uploads straight to S3; the image shows; the download works and the link is refused after expiry; the replaced object is gone (a noncurrent version remains if bucket versioning is on); deleting the application leaves no `product-content/{id}/` objects. No credential appears in the browser or any response.

## Automated coverage
Manual (needs a real bucket; the automated tests use an in-memory store and the real AWS SDK signer).

## Actual Result
Not run yet: needs a test bucket (open item in C81).

## Status
Not run

## Linked Defect (if failed)
