# TC-TEN-139: My profile — a listed platform administrator without a record or permission gets exists false

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-139 |
| Requirement ID (required) | [REQ-TEN-011](../../../docs/02-requirements/FRD/my-profile/requirement.md) (BR-PRF-026, BR-PRF-002) |
| Acceptance Criterion | [AC-19](../../../docs/02-requirements/FRD/my-profile/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-011](TESTPLAN-TEN-011.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
The registry holds an enabled entry whose `keycloakSub` equals the `sub` of a login. That login holds no platform permission (not `MANAGE_REGISTRATIONS`) and has no customer record.

## Steps
1. Sign in as that login and call `GET /me/profile`.
2. Call `GET /me/profile/completion`.
3. Open `/account/profile`.

## Expected Result
`GET /me/profile` answers 200 with `exists` false, built from the token (email, given name, family name), everything else null and completion 0 of 11 with all 11 items missing; it is not 404. The completion call answers 200 `{exists:false, percent:0, filled:0, total:11, missing:[all 11 keys]}`. The page shows the optional "Create my profile" banner, not the load-error state.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/profile/service/ProfileServiceTest.java` — `aListedAdminWithoutTheAdminPermissionGetsExistsFalseCreatesTheOwnRecordThenAddsAPhoto`
- `backend/src/test/java/com/vyoog/eisplatform/modules/profile/controller/ProfileControllerTest.java` — `aRegistryAdminWithoutTheAdminPermissionGetsExistsFalseCreatesTheRecordAndAddsAPhoto`

## Actual Result
Not run yet.

## Status
Not Run
