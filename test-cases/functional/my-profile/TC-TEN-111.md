# TC-TEN-111: My profile — a platform administrator without a record is not forced to create one

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-111 |
| Requirement ID (required) | [REQ-TEN-011](../../../docs/02-requirements/FRD/my-profile/requirement.md) (BR-PRF-002, BR-PRF-003, BR-PRF-019) |
| Acceptance Criterion | [AC-8](../../../docs/02-requirements/FRD/my-profile/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-011](TESTPLAN-TEN-011.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A platform administrator with no customer record.

## Steps
1. Sign in and open the admin pages, the catalog and `/account/security`.
2. Call `GET /me/profile` and `GET /me/profile/completion`.
3. Try `PUT /me/profile/photo` with a valid image and `DELETE /me/profile/photo`.
4. Open `/account/profile`.

## Expected Result
No redirect or prompt appears and no page is blocked. The API returns `exists` false with email, first and last name from the token, everything else null and completion 0 of 11 with all 11 items missing. The page shows the banner "Your customer profile has not been created. It is optional." and a Create my profile action; the global bar is not shown on any page. The photo upload and the removal answer 409 "Create your profile before adding a photo" and the page keeps the photo actions disabled until the record exists.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/profile/service/ProfileServiceTest.java` — `aPlatformAdminWithoutARecordGetsAProfileBuiltFromTheLogin`, `aPlatformAdminWithoutARecordCannotAddAPhotoYet`
- `backend/src/test/java/com/vyoog/eisplatform/modules/profile/controller/ProfileControllerTest.java` — `aNonAdminWithoutARecordGets404AndAPlatformAdminGetsExistsFalseThenCreatesTheRecord`
- `frontend/src/pages/ProfilePage.test.tsx` — `says the profile is optional, prefilled with the sign-in names, and shows no progress nagging`
- `frontend/src/components/profile/ProfileCompletionBar.test.tsx` — `hides the bar for a person with no record and does not poll`

## Actual Result
Not run yet.

## Status
Not Run
