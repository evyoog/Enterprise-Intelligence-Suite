# TC-TEN-109: My profile — anonymous calls are refused

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-109 |
| Requirement ID (required) | [REQ-TEN-011](../../../docs/02-requirements/FRD/my-profile/requirement.md) (BR-PRF-001) |
| Acceptance Criterion | [AC-7](../../../docs/02-requirements/FRD/my-profile/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-011](TESTPLAN-TEN-011.md) |
| Priority | P0 |
| Type | Security |
| Automated | Yes |

## Preconditions
No sign-in token.

## Steps
1. Call every endpoint of the profile API (`GET`/`PUT /me/profile`, `GET /me/profile/completion`, `PUT`/`DELETE`/`GET /me/profile/photo`, and both admin paths) without a token and with an expired token.

## Expected Result
Each call answers 401 and no data or photo is returned.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/profile/controller/ProfileControllerTest.java` — `everyProfileEndpointNeedsASignedInCaller`
- `backend/src/test/java/com/vyoog/eisplatform/modules/profile/service/ProfileServiceTest.java` — `anApiKeyIsNeverAPersonAndCannotUseTheProfile`

## Actual Result
Not run yet.

## Status
Not Run
