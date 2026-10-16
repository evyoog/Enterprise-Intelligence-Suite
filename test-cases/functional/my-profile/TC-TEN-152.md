# TC-TEN-152: My profile — the registry re-reads its source only after the refresh interval

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-152 |
| Requirement ID (required) | [REQ-TEN-011](../../../docs/02-requirements/FRD/my-profile/requirement.md) (BR-PRF-030) |
| Acceptance Criterion | [AC-26](../../../docs/02-requirements/FRD/my-profile/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-011](TESTPLAN-TEN-011.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A registry with a fixed, injectable clock and a source whose content can be changed between reads; interval 60 seconds.

## Steps
1. Match a login against the first content.
2. Change the source and match again before 60 seconds have passed.
3. Advance the clock beyond 60 seconds and match again.

## Expected Result
The source is read once at first use and not again inside the interval (the old answer stands); after the interval the next use reads the new content and the answer changes. No scheduler is involved; a failed first load is retried only after the interval. The first load blocks other callers, a later refresh never does.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/platformadmin/service/PlatformAdminRegistryTest.java` — `theSourceIsReReadOnlyAfterTheRefreshInterval`, `aFailedFirstLoadIsRetriedOnlyAfterTheInterval`

## Actual Result
Not run yet.

## Status
Not Run
