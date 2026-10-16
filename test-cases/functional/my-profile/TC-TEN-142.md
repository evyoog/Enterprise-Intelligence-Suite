# TC-TEN-142: My profile — the registry matches by Keycloak user id

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-142 |
| Requirement ID (required) | [REQ-TEN-011](../../../docs/02-requirements/FRD/my-profile/requirement.md) (BR-PRF-028) |
| Acceptance Criterion | [AC-22](../../../docs/02-requirements/FRD/my-profile/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-011](TESTPLAN-TEN-011.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An enabled entry with a non-blank `keycloakSub` equal to a login's `sub`, whose `email` is different from the login's email (or empty).

## Steps
1. Present a token whose `sub` equals the entry's `keycloakSub` to the registry (profile call without a record or permission).
2. Present a token with a different `sub` and the entry's email.

## Expected Result
The first token is recognised as a platform administrator (profile answers as in TC-TEN-139). The second is not recognised through the id; it is recognised only if the email rule of TC-TEN-143 also holds.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/platformadmin/service/PlatformAdminRegistryTest.java` — `aLoginMatchesBySubWithoutAnyEmail`

## Actual Result
Not run yet.

## Status
Not Run
