# TC-TEN-157: My profile — a holder of MANAGE_REGISTRATIONS behaves as before

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-157 |
| Requirement ID (required) | [REQ-TEN-011](../../../docs/02-requirements/FRD/my-profile/requirement.md) (BR-PRF-026, BR-PRF-027, BR-PRF-020) |
| Acceptance Criterion | [AC-29](../../../docs/02-requirements/FRD/my-profile/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-011](TESTPLAN-TEN-011.md) |
| Priority | P0 |
| Type | Regression |
| Automated | Yes |

## Preconditions
A login holding `MANAGE_REGISTRATIONS` that is not listed in the registry (the shipped file), once with a customer record and once without.

## Steps
1. Run the profile cases TC-TEN-110 to TC-TEN-113 and TC-TEN-130 for that login.
2. Call `GET`/`PUT /admin/registrations/individuals/{customerId}/profile`.

## Expected Result
Every outcome is the same as before the registry existed: `exists:false` and creating the own record without a record, role `PLATFORM_ADMIN`, 409 on the photo before the record exists, the admin edit paths working. The existing profile tests pass unchanged apart from the new constructor dependency.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/profile/service/ProfileServiceTest.java` — `aListedAdminWhoAlreadyHasARecordIsShownAsPlatformAdminWhileAnUnlistedPersonIsNot` (an unlisted person is not changed)
- The existing `ProfileServiceTest` and `ProfileControllerTest` cases for a holder of `MANAGE_REGISTRATIONS` (TC-TEN-110 to TC-TEN-113, TC-TEN-130) run unchanged; full `mvn -B verify`: 1023 tests, 0 failures.

## Actual Result
Not run yet.

## Status
Not Run
