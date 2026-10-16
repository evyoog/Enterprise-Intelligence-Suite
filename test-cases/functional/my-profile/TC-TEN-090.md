# TC-TEN-090: My profile — every role sees their own profile

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-090 |
| Requirement ID (required) | [REQ-TEN-011](../../../docs/02-requirements/FRD/my-profile/requirement.md) (BR-PRF-001, BR-PRF-022) |
| Acceptance Criterion | [AC-1](../../../docs/02-requirements/FRD/my-profile/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-011](TESTPLAN-TEN-011.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A platform administrator with a customer record, an organization administrator, a member and an individual, all signed in.

## Steps
1. As each of the four people call `GET /me/profile` and open `/account/profile`.
2. Compare the role, organization name and code, status and member-since with the person's record.

## Expected Result
Each person gets their own `ProfileDto` with `exists` true, the right `account.role` (`PLATFORM_ADMIN`, `ORG_ADMIN`, `MEMBER`, `INDIVIDUAL`) and the page shows the tabs Personal, Work, Address, Online and Account. `account.status` is the membership status (ACTIVE, SUSPENDED or INACTIVE; the active membership is preferred, otherwise the most recent) and `memberSince` its joined date. An individual has no organization name, code or status, and member-since is the date the account was created. A platform administrator who also has a customer row still shows the role PLATFORM_ADMIN.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/profile/service/ProfileServiceTest.java` — `theAccountSectionShowsRoleOrganizationAndMembership`
- `backend/src/test/java/com/vyoog/eisplatform/modules/profile/controller/ProfileControllerTest.java` — `getReturnsTheCallersOwnProfileWithExplicitNulls`
- `frontend/src/pages/ProfilePage.test.tsx` — `shows who the person is, the completion status and the five tabs`, `switches tabs and fills each with its own fields`

## Actual Result
Not run yet.

## Status
Not Run
