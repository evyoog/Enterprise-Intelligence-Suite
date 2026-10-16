# TC-TEN-153: My profile — an invalid or unreadable first load leaves the registry empty

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-153 |
| Requirement ID (required) | [REQ-TEN-011](../../../docs/02-requirements/FRD/my-profile/requirement.md) (BR-PRF-031) |
| Acceptance Criterion | [AC-27](../../../docs/02-requirements/FRD/my-profile/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-011](TESTPLAN-TEN-011.md) |
| Priority | P0 |
| Type | Security |
| Automated | Yes |

## Preconditions
A source whose first read returns invalid JSON (or throws an error), and a configuration with an unsupported location.

## Steps
1. Use the registry for a login that would match a valid file.
2. Read the number of enabled entries.

## Expected Result
The registry is empty: the login is not recognised and the enabled count is 0 (fail closed). The unsupported location does not stop the application from starting. A WARN line names only the source type and the error class. A failed first load is retried only after the next refresh interval.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/platformadmin/service/PlatformAdminRegistryTest.java` — `invalidJsonOnTheFirstLoadMeansNobodyIsAnAdmin`, `aFailingSourceOnTheFirstLoadMeansNobodyIsAnAdmin`
- `backend/src/test/java/com/vyoog/eisplatform/modules/platformadmin/service/PlatformAdminSourceTest.java` — `anUnsupportedLocationFailsClosedInsteadOfStoppingTheApplication`

## Actual Result
Not run yet.

## Status
Not Run
