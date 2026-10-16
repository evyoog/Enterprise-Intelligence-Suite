# TC-TEN-103: My profile — every invalid field is reported at once and nothing is saved

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-103 |
| Requirement ID (required) | [REQ-TEN-011](../../../docs/02-requirements/FRD/my-profile/requirement.md) (BR-PRF-012) |
| Acceptance Criterion | [AC-5](../../../docs/02-requirements/FRD/my-profile/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-011](TESTPLAN-TEN-011.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A signed-in person with saved values.

## Steps
1. Send one `PUT /me/profile` with an invalid first name, an invalid mobile, an invalid postal code and an invalid website.
2. Read the profile again.

## Expected Result
HTTP 400 with `{status:400, message:"Validation failed", fieldErrors}` holding exactly the four keys `personal.firstName`, `personal.mobile`, `address.postalCode`, `online.websiteUrl` with English messages. The stored profile is unchanged. Errors raised elsewhere in the platform keep their old `{status, message}` shape.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/profile/service/ProfileValidatorTest.java` — `everyErrorIsReportedAtOnceUnderItsSectionDotFieldKey`, `theValidatorThrowsAValidationExceptionWithStatus400`
- `backend/src/test/java/com/vyoog/eisplatform/modules/profile/service/ProfileServiceTest.java` — `aFailedValidationSavesNothingAndCarriesEveryFieldError`
- `backend/src/test/java/com/vyoog/eisplatform/modules/profile/controller/ProfileControllerTest.java` — `invalidInputIs400WithOneFieldErrorPerBadField`, `otherErrorsKeepThePlatformsStandardBodyOutsideTheProfileControllers`

## Actual Result
Not run yet.

## Status
Not Run
