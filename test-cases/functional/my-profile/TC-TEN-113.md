# TC-TEN-113: My profile — token email already linked to another login gives 409

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-113 |
| Requirement ID (required) | [REQ-TEN-011](../../../docs/02-requirements/FRD/my-profile/requirement.md) (BR-PRF-003) |
| Acceptance Criterion | [AC-9](../../../docs/02-requirements/FRD/my-profile/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-011](TESTPLAN-TEN-011.md) |
| Priority | P1 |
| Type | Security |
| Automated | Yes |

## Preconditions
A platform administrator whose token email equals (in any letter case) the email of an existing customer row, linked to a different sign-in or not linked at all.

## Steps
1. Call `PUT /me/profile` with valid data.

## Expected Result
HTTP 409 and no row is created or changed (any customer row with that email blocks the creation); the other person's record is untouched.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/profile/service/ProfileServiceTest.java` — `aPlatformAdminWhoseEmailBelongsToAnotherLoginGets409AndNothingIsCreated`
- `backend/src/test/java/com/vyoog/eisplatform/modules/profile/controller/ProfileControllerTest.java` — `anAdminWhoseEmailBelongsToAnotherLoginGets409`

## Actual Result
Not run yet.

## Status
Not Run
