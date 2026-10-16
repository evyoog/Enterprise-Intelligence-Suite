# TC-TEN-148: My profile — the registry file is read leniently

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-148 |
| Requirement ID (required) | [REQ-TEN-011](../../../docs/02-requirements/FRD/my-profile/requirement.md) (BR-PRF-032) |
| Acceptance Criterion | [AC-24](../../../docs/02-requirements/FRD/my-profile/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-011](TESTPLAN-TEN-011.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A JSON file with an unknown property at the top level and inside an entry, an entry without `enabled`, and the same entry twice.

## Steps
1. Load the file into the registry and count enabled entries.
2. Match a login against the entry without `enabled`.

## Expected Result
The file loads without error; unknown properties are ignored; the entry without `enabled` counts as enabled and matches; the duplicate does not change the result.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/platformadmin/service/PlatformAdminRegistryTest.java` — `aDisabledEntryIsIgnoredAndAMissingEnabledMeansEnabled`, `unknownJsonFieldsAreIgnoredAndMissingListsAreEmpty`
- A duplicate entry has no dedicated test (it is harmless by construction).

## Actual Result
Not run yet.

## Status
Not Run
