# TC-TEN-107: My profile — a person cannot read another person's profile

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-107 |
| Requirement ID (required) | [REQ-TEN-011](../../../docs/02-requirements/FRD/my-profile/requirement.md) (BR-PRF-001) |
| Acceptance Criterion | [AC-7](../../../docs/02-requirements/FRD/my-profile/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-011](TESTPLAN-TEN-011.md) |
| Priority | P0 |
| Type | Security |
| Automated | Yes |

## Preconditions
Two members, A and B, in the same organization, and an organization administrator.

## Steps
1. Sign in as A and call `GET /me/profile` with a query `?customerId=<B>` and `?id=<B>`.
2. Call `GET /me/profile/photo` the same way; call `GET /me/profile/completion`.
3. As the organization administrator repeat the calls for B.
4. Call `GET /me/profile` with an API-key token instead of a person's token.

## Expected Result
Every call returns the caller's own data (the extra id is ignored); B's fields and photo are never returned to A or to the organization administrator. There is no `/me` route that takes a customer id. The API-key token is refused with 403.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/profile/service/ProfileServiceTest.java` — `aPersonCanNeverReadOrWriteAnotherPersonsProfile`, `anApiKeyIsNeverAPersonAndCannotUseTheProfile`
- `backend/src/test/java/com/vyoog/eisplatform/modules/profile/controller/ProfileControllerTest.java` — `aPhotoIsNeverServedToAnotherPerson`
- `frontend/src/api/profileApi.test.ts` — `uses the /me endpoints for the caller and never sends an id`

## Actual Result
Not run yet.

## Status
Not Run
