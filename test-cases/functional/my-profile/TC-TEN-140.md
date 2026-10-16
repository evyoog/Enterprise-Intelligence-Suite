# TC-TEN-140: My profile — a listed platform administrator creates their own record

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-140 |
| Requirement ID (required) | [REQ-TEN-011](../../../docs/02-requirements/FRD/my-profile/requirement.md) (BR-PRF-026, BR-PRF-003, BR-PRF-021, BR-PRF-022) |
| Acceptance Criterion | [AC-20](../../../docs/02-requirements/FRD/my-profile/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-011](TESTPLAN-TEN-011.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
As TC-TEN-139. A second case: another customer row already has the login's email in a different letter case and is linked to a different login.

## Steps
1. Call `PUT /me/profile` with a valid body (names typed as in the token).
2. Call `GET /me/profile`.
3. Repeat the `PUT` for the login whose email already belongs to another login's customer row.
4. Change the typed first name in a later `PUT` for the created record.

## Expected Result
The first `PUT` creates the customer row (email from the token, the sign-in linked, status COMPLETED), is audited `PROFILE_CREATED` and returns the full profile with `exists` true and `account.role` `PLATFORM_ADMIN`; Keycloak is not called when the typed names equal the token's names. A token without an email claim is refused with 400 and nothing is created. The duplicate-email case answers 409 and creates nothing. The later name change is mirrored to Keycloak as for any person (BR-PRF-015). Creating the record was never required for any other call.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/profile/service/ProfileServiceTest.java` — `aListedAdminWithoutTheAdminPermissionGetsExistsFalseCreatesTheOwnRecordThenAddsAPhoto`, `aListedAdminMatchedByVerifiedEmailCreatesTheirRecordButAnUnverifiedEmailDoesNot`, `aListedAdminWhoseEmailBelongsToAnotherLoginStillGets409AndNothingIsCreated`, `aListedAdminWhoAlreadyHasARecordIsShownAsPlatformAdminWhileAnUnlistedPersonIsNot`
- `backend/src/test/java/com/vyoog/eisplatform/modules/profile/controller/ProfileControllerTest.java` — `aRegistryAdminWithoutTheAdminPermissionGetsExistsFalseCreatesTheRecordAndAddsAPhoto`

## Actual Result
Not run yet.

## Status
Not Run
