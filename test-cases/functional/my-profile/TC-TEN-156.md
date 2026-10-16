# TC-TEN-156: My profile — the shipped registry file contains no real identity and grants nothing

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-156 |
| Requirement ID (required) | [REQ-TEN-011](../../../docs/02-requirements/FRD/my-profile/requirement.md) (BR-PRF-032) |
| Acceptance Criterion | [AC-28](../../../docs/02-requirements/FRD/my-profile/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-011](TESTPLAN-TEN-011.md) |
| Priority | P0 |
| Type | Security |
| Automated | Yes |

## Preconditions
The file `backend/src/main/resources/config/platform-admins.json` as shipped.

## Steps
1. Parse it with the registry parser.
2. Match a login with the example email and a login with an empty `sub`.
3. Read the file for passwords, tokens or real addresses.

## Expected Result
It parses as version 1 with a description and one example entry that is `enabled: false` with the placeholder email and an empty `keycloakSub`; the enabled count is 0 and no login is recognised; the file holds no password, token or real address.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/platformadmin/service/PlatformAdminSourceTest.java` — `theShippedDefaultFileParsesAndGrantsNothing`

## Actual Result
Not run yet.

## Status
Not Run
