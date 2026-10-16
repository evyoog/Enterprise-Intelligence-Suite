# TC-TEN-146: My profile — an API key is never matched, even when listed

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-146 |
| Requirement ID (required) | [REQ-TEN-011](../../../docs/02-requirements/FRD/my-profile/requirement.md) (BR-PRF-027, BR-PRF-001) |
| Acceptance Criterion | [AC-23](../../../docs/02-requirements/FRD/my-profile/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-011](TESTPLAN-TEN-011.md) |
| Priority | P0 |
| Type | Security |
| Automated | Yes |

## Preconditions
A registry entry whose `keycloakSub` or email equals those carried by an API-key token (claim `api_key_id`).

## Steps
1. Call each profile endpoint with that API-key token.

## Expected Result
Each call answers 403 ("An API key cannot be used for a personal profile"); the API key is refused before the registry is consulted.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/platformadmin/service/PlatformAdminRegistryTest.java` — `anApiKeyTokenIsNeverMatched`
- `backend/src/test/java/com/vyoog/eisplatform/modules/profile/service/ProfileServiceTest.java` — `aListedAdminWithAnApiKeyClaimIsForbiddenOnEveryOwnProfileCall`
- `backend/src/test/java/com/vyoog/eisplatform/modules/profile/controller/ProfileControllerTest.java` — `aRegistryAdminUsingAnApiKeyIsForbidden`

## Actual Result
Not run yet.

## Status
Not Run
