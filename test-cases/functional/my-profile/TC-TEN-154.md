# TC-TEN-154: My profile — a failed refresh keeps the last good copy

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-154 |
| Requirement ID (required) | [REQ-TEN-011](../../../docs/02-requirements/FRD/my-profile/requirement.md) (BR-PRF-031) |
| Acceptance Criterion | [AC-27](../../../docs/02-requirements/FRD/my-profile/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-011](TESTPLAN-TEN-011.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A registry that has loaded a valid file and recognises a login.

## Steps
1. Make the source return invalid JSON (or throw), advance the clock beyond the interval and match the login again.
2. Make the source valid again with one entry removed, advance the clock and match again.

## Expected Result
While the source is invalid the login is still recognised (last good copy kept). After the source is valid again the new content applies and the removed entry no longer matches.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/platformadmin/service/PlatformAdminRegistryTest.java` — `invalidJsonOrAFailureOnRefreshKeepsTheLastGoodList`
- `backend/src/test/java/com/vyoog/eisplatform/modules/platformadmin/service/PlatformAdminSourceTest.java` — `anS3ErrorReachesTheRegistryWhichKeepsTheLastGoodList`

## Actual Result
Not run yet.

## Status
Not Run
