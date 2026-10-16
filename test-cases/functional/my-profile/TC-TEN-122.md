# TC-TEN-122: My profile — Keycloak is not called when the names do not change

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-122 |
| Requirement ID (required) | [REQ-TEN-011](../../../docs/02-requirements/FRD/my-profile/requirement.md) (BR-PRF-015) |
| Acceptance Criterion | [AC-12](../../../docs/02-requirements/FRD/my-profile/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-011](TESTPLAN-TEN-011.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
Keycloak is made to fail for any call.

## Steps
1. Change only the city and the website and save.
2. Change only the preferred name, the alternate phone or the bio and save.
3. Save with the same first and last name written with extra spaces (`  Ana  `).

## Expected Result
All saves succeed without any Keycloak call (the names are unchanged after normalisation; mirroring is for first and last name only, BR-PRF-015). The last save changes nothing and also writes no audit entry.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/profile/service/ProfileServiceTest.java` — `savingWithoutChangingTheNameDoesNotCallKeycloak`, `aPersonNotLinkedToKeycloakYetIsSavedWithoutACall`

## Actual Result
Not run yet.

## Status
Not Run
