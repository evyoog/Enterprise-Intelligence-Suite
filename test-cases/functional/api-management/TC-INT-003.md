# TC-INT-003: Revoked, expired and unknown keys are refused; users manage only their own keys

| Field | Value |
|---|---|
| Test Case ID (required) | TC-INT-003 |
| Requirement ID (required) | [REQ-INT-001](../../../docs/02-requirements/FRD/api-management/requirement.md) (C61) |
| Acceptance Criterion | [AC-3](../../../docs/02-requirements/FRD/api-management/acceptance-criteria.md), [AC-4](../../../docs/02-requirements/FRD/api-management/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-INT-001](TESTPLAN-INT-001.md) |
| Priority | P1 |
| Type | Security |
| Automated | Yes |

## Preconditions
Users A and B; a key of A.

## Steps
1. B revokes A's key.
2. A revokes the key; revokes it again.
3. Call the API with the revoked key and with an unknown key.
4. Create a key with an expiry in the past.

## Expected Result
B gets 404 and A's key stays Active; A's revoke succeeds, the second is 409; the revoked and unknown keys get 401 `API_KEY_INVALID`; the revoked use is audited (`API_KEY_REVOKED_USED`); a past expiry is 400. A key cannot create another key (403); an `Authorization` header always wins over `X-API-Key`.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/config/ApiKeyAuthenticationTest.java` — `anInvalidOrRevokedKeyIsRejectedAndTheRevokedUseIsAudited`, `usersManageOnlyTheirOwnKeys`, `aKeyCannotCreateAnotherKey`, `atMostTenActiveKeysAndTheExpiryMustBeInTheFuture`
- `backend/src/test/java/com/vyoog/eisplatform/config/ApiKeyAuthenticationFilterTest.java`

## Actual Result
The automated tests passed on 2026-10-03.

## Status
Passed (automated run 2026-10-03)

## Linked Defect (if failed)
None.
