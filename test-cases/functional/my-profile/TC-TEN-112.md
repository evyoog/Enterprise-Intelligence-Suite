# TC-TEN-112: My profile — a platform administrator creates their own record

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-112 |
| Requirement ID (required) | [REQ-TEN-011](../../../docs/02-requirements/FRD/my-profile/requirement.md) (BR-PRF-003, BR-PRF-021) |
| Acceptance Criterion | [AC-9](../../../docs/02-requirements/FRD/my-profile/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-011](TESTPLAN-TEN-011.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A platform administrator with no customer record and a token email not used by any customer.

## Steps
1. Open `/account/profile` and press Create my profile; check the names are prefilled from the sign-in.
2. Add a mobile number and save.
3. Reload and call `GET /me/profile`; check the audit log and the call made to Keycloak.
4. Repeat with a token that has no email claim.

## Expected Result
A customer row now exists with the token's email, the sign-in linked and status COMPLETED; `exists` is true and the page behaves as for everyone else. The audit log has `PROFILE_CREATED` and no values. Keycloak is updated at creation only when the names typed differ from the token's given and family name. A token without an email claim is refused with 400 and nothing is created. A first save with blank names is refused.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/profile/service/ProfileServiceTest.java` — `aPlatformAdminCreatesTheirOwnRecordOnFirstSave`, `aPlatformAdminWhoChangesTheirNameWhileCreatingTheRecordIsMirrored`, `aLoginWithoutAnEmailCannotCreateARecord`, `auditRecordsNameTheChangedSectionsAndNeverTheValues`
- `backend/src/test/java/com/vyoog/eisplatform/modules/profile/controller/ProfileControllerTest.java` — `aNonAdminWithoutARecordGets404AndAPlatformAdminGetsExistsFalseThenCreatesTheRecord`
- `frontend/src/pages/ProfilePage.test.tsx` — `creates the record from the banner and then works like any other profile`, `does not create the record when the names are blank, and shows the problem`

## Actual Result
Not run yet.

## Status
Not Run
