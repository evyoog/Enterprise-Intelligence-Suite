# TC-TEN-125: My profile — what counts as filled; one computation; directory completion unchanged

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-125 |
| Requirement ID (required) | [REQ-TEN-011](../../../docs/02-requirements/FRD/my-profile/requirement.md) (BR-PRF-017, BR-PRF-018) |
| Acceptance Criterion | [AC-13](../../../docs/02-requirements/FRD/my-profile/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-011](TESTPLAN-TEN-011.md) |
| Priority | P1 |
| Type | Regression |
| Automated | Yes |

## Preconditions
A person with a mobile of whitespace only, a job title filled and a photo row.

## Steps
1. Read `GET /me/profile` and `GET /me/profile/completion`.
2. Remove the photo and read both again.
3. Open the same person in the organizations directory (REQ-TEN-007).

## Expected Result
The whitespace mobile is not counted, the photo counts only while its row exists, and both endpoints return identical figures. The directory still shows its own completion (BR-DIR-005, five items) unchanged; the two may differ.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/profile/service/PersonProfileCompletenessTest.java` — `blankValuesDoNotCountAndNamesAndEmailAreNotPartOfIt`, `thePhotoIsOneOfTheEleven`
- `backend/src/test/java/com/vyoog/eisplatform/modules/profile/service/ProfileServiceTest.java` — `completionCountsFilledItemsAndThePhoto`, `completionForASignedInPersonWithoutARecordIsExistsFalseNotAnError`
- `backend/src/test/java/com/vyoog/eisplatform/modules/profile/controller/ProfileControllerTest.java` — `putSavesAndNormalisesAndTheCompletionEndpointFollows`

## Actual Result
Not run yet.

## Status
Not Run
