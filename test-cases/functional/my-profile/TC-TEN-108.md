# TC-TEN-108: My profile — a person cannot edit another person's profile

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-108 |
| Requirement ID (required) | [REQ-TEN-011](../../../docs/02-requirements/FRD/my-profile/requirement.md) (BR-PRF-001, BR-PRF-020) |
| Acceptance Criterion | [AC-7](../../../docs/02-requirements/FRD/my-profile/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-011](TESTPLAN-TEN-011.md) |
| Priority | P0 |
| Type | Security |
| Automated | Yes |

## Preconditions
Members A and B, an organization administrator, and a platform administrator without `MANAGE_REGISTRATIONS` (if such a role exists).

## Steps
1. As A send `PUT /me/profile` and `PUT /me/profile/photo` with a body or part naming B's id.
2. As A and as the organization administrator call `PUT /admin/registrations/individuals/<B>/profile`.
3. Read B's profile as B.

## Expected Result
A's changes go only to A. The admin path answers 403 for A and for the organization administrator. B's profile and photo are unchanged.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/profile/service/ProfileServiceTest.java` — `aPersonCanNeverReadOrWriteAnotherPersonsProfile`
- `backend/src/test/java/com/vyoog/eisplatform/modules/profile/controller/ProfileControllerTest.java` — `theAdminEndpointsNeedTheRegistrationsPermission`

## Actual Result
Not run yet.

## Status
Not Run
