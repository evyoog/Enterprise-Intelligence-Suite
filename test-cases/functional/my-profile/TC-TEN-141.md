# TC-TEN-141: My profile — a listed platform administrator and the photo

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-141 |
| Requirement ID (required) | [REQ-TEN-011](../../../docs/02-requirements/FRD/my-profile/requirement.md) (BR-PRF-026, BR-PRF-014) |
| Acceptance Criterion | [AC-20](../../../docs/02-requirements/FRD/my-profile/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-011](TESTPLAN-TEN-011.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
As TC-TEN-139, then (second part) the record has been created as in TC-TEN-140.

## Steps
1. With no record, call `PUT /me/profile/photo` with a valid image and `DELETE /me/profile/photo`.
2. Create the record (TC-TEN-140), then call `PUT /me/profile/photo` with a valid image.
3. Call `GET /me/profile/completion`.

## Expected Result
Before the record exists both photo calls answer 409 "Create your profile before adding a photo" (not 404). After the record exists the upload is accepted under the normal photo rules and audited `PROFILE_PHOTO_UPDATED`. The completion call behaves as before (never 404; the photo now counts).

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/profile/service/ProfileServiceTest.java` — `aListedAdminWithoutTheAdminPermissionGetsExistsFalseCreatesTheOwnRecordThenAddsAPhoto`
- `backend/src/test/java/com/vyoog/eisplatform/modules/profile/controller/ProfileControllerTest.java` — `aRegistryAdminWithoutTheAdminPermissionGetsExistsFalseCreatesTheRecordAndAddsAPhoto`

## Actual Result
Not run yet.

## Status
Not Run
