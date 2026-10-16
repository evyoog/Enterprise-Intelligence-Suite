# TC-TEN-105: My profile — email, role and status cannot be changed; language and time zone are not duplicated

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-105 |
| Requirement ID (required) | [REQ-TEN-011](../../../docs/02-requirements/FRD/my-profile/requirement.md) (BR-PRF-004, BR-PRF-022) |
| Acceptance Criterion | [AC-6](../../../docs/02-requirements/FRD/my-profile/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-011](TESTPLAN-TEN-011.md) |
| Priority | P0 |
| Type | Security |
| Automated | Yes |

## Preconditions
A signed-in member.

## Steps
1. Send `PUT /me/profile` with extra fields `email`, `role`, `status`, `keycloakSub` and `photo` in the body.
2. Open the Account tab and look for editable email, role, language or time-zone fields.
3. Use the Preferences link.

## Expected Result
The stored email, role, status and sign-in link are unchanged. The Account tab shows them read-only, has no language or time-zone control, and its link opens `/account/preferences`.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/profile/service/ProfileServiceTest.java` — `theEmailIsNeverChangedByASave`
- `frontend/src/pages/ProfilePage.test.tsx` — `keeps the Account tab read-only, with links to security and preferences and the organization pages`

## Actual Result
Not run yet.

## Status
Not Run
