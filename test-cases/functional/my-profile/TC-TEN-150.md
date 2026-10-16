# TC-TEN-150: My profile — the registry reads an s3:// location through the fetch seam

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-150 |
| Requirement ID (required) | [REQ-TEN-011](../../../docs/02-requirements/FRD/my-profile/requirement.md) (BR-PRF-029) |
| Acceptance Criterion | [AC-25](../../../docs/02-requirements/FRD/my-profile/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-011](TESTPLAN-TEN-011.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
The S3 source built with a test fetcher in place of the AWS client (no AWS access).

## Steps
1. Read `s3://example-bucket/config/platform-admins.json`.
2. Read a location with a key containing several path segments, and a malformed one (no bucket or no key).
3. Make the fetcher throw an error.

## Expected Result
The fetcher receives the bucket `example-bucket` and the key `config/platform-admins.json` (the key keeps every path segment); a malformed `s3://` location is an error of the source: it does not stop the application from starting and is handled like any failing source (first load: the registry stays empty; refresh: the last good list is kept); the fetcher's error is handled the same way (TC-TEN-153 and TC-TEN-154). The source reports its scheme `s3`. No access key is read from configuration.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/platformadmin/service/PlatformAdminSourceTest.java` — `theS3SourceSplitsBucketAndKeyAndFetchesThroughTheSeam`, `aMalformedS3LocationIsRejected`, `anS3ErrorReachesTheRegistryWhichKeepsTheLastGoodList`

## Actual Result
Not run yet.

## Status
Not Run
