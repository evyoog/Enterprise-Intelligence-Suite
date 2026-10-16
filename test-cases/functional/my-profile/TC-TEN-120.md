# TC-TEN-120: My profile — a name change is mirrored to Keycloak

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-120 |
| Requirement ID (required) | [REQ-TEN-011](../../../docs/02-requirements/FRD/my-profile/requirement.md) (BR-PRF-015) |
| Acceptance Criterion | [AC-12](../../../docs/02-requirements/FRD/my-profile/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-011](TESTPLAN-TEN-011.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A person with a Keycloak user; the Keycloak admin client is faked in tests.

## Steps
1. Change the first name and last name and save.
2. Check the call made to Keycloak and the stored customer.

## Expected Result
The Keycloak user receives the new first and last name once, the customer row has the same names and the save answers 200.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/profile/service/ProfileServiceTest.java` — `aNameChangeIsMirroredToKeycloak`

## Actual Result
Not run yet.

## Status
Not Run
