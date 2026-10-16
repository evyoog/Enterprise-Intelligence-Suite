# TC-TEN-151: My profile — the registry reads a live S3 object

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-151 |
| Requirement ID (required) | [REQ-TEN-011](../../../docs/02-requirements/FRD/my-profile/requirement.md) (BR-PRF-029, BR-PRF-031) |
| Acceptance Criterion | [AC-25](../../../docs/02-requirements/FRD/my-profile/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-011](TESTPLAN-TEN-011.md) |
| Priority | P1 |
| Type | Functional |
| Automated | No (manual check, needs a real bucket) |

## Preconditions
A private bucket and object created following [platform-admin-registry.md](../../../docs/08-architecture/security/platform-admin-registry.md) section 11; the backend runs under a role that holds `s3:GetObject` on that object only; `EIS_PLATFORM_ADMINS_LOCATION` and `EIS_PLATFORM_ADMINS_S3_REGION` are set.

## Steps
1. Start the backend and read the registry log line.
2. Sign in as a listed login with no record and open `/account/profile`.
3. Set the entry to `enabled: false` in the S3 object, wait one refresh interval and reload the profile.
4. Remove the role's `s3:GetObject` permission and restart (or point to a missing key).

## Expected Result
The log line names the source type `s3` and the number of enabled entries; the listed login sees the optional "Create my profile" banner; after the interval the disabled login is no longer recognised and no redeploy was needed; with no access the registry is empty (fail closed) and a WARN line names only `s3` and the error class.

## Automated coverage
- None: manual check.

## Actual Result
Not run yet.

## Status
Not Run
