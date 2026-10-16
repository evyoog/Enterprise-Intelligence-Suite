# TC-TEN-121: My profile — a Keycloak failure rolls the whole save back

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-121 |
| Requirement ID (required) | [REQ-TEN-011](../../../docs/02-requirements/FRD/my-profile/requirement.md) (BR-PRF-015) |
| Acceptance Criterion | [AC-12](../../../docs/02-requirements/FRD/my-profile/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-011](TESTPLAN-TEN-011.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
Keycloak is made to fail or time out for the next name update.

## Steps
1. Change the first name and also the city and the bio; save.
2. Read the profile and the audit log; look at the screen.

## Expected Result
HTTP 502; no field changed (not even the city and bio); no audit entry for the update and no new `profile_updated_at`; the screen shows "Your name could not be updated at the sign-in service, so nothing was saved. Please try again later." and keeps the typed values so a retry works once Keycloak is back.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/profile/service/ProfileServiceTest.java` — `ifKeycloakRefusesTheWholeSaveIsRolledBackWith502`
- `backend/src/test/java/com/vyoog/eisplatform/modules/profile/controller/ProfileControllerTest.java` — `keycloakFailureIs502WithTheStandardBody`
- `frontend/src/pages/ProfilePage.test.tsx` — `tells the person nothing was saved when the sign-in service could not be updated (502)`

## Actual Result
Not run yet.

## Status
Not Run
