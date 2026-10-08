# TC-TEN-049: Invite user — existing account and new account

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-049 |
| Requirement ID (required) | [REQ-TEN-008](../../../docs/02-requirements/FRD/invite-user/requirement.md) |
| Acceptance Criterion | [AC-6](../../../docs/02-requirements/FRD/invite-user/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-008](TESTPLAN-TEN-008.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Steps
1. An existing person signs in and accepts.
2. A new person opens the link, enters name and password.

## Expected Result
The existing account joins with the invited role; a different signed-in email is refused. The new person gets a customer and an enabled Keycloak user and joins.

## Automated coverage
- `backend/.../invitation/service/InvitationServiceTest.java` — `anExistingAccountAcceptsAfterSignInAndNeedsTheSameEmail`, `aNewPersonCreatesAnAccountFromTheInvitationAndJoins`
- `frontend/src/pages/InvitationPage.test.tsx`

## Actual Result
The automated tests passed on 2026-10-08.

## Status
Passed
