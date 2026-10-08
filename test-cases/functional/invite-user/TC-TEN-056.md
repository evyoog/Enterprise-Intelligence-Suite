# TC-TEN-056: Invite user — real email and sign-in (manual)

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-056 |
| Requirement ID (required) | [REQ-TEN-008](../../../docs/02-requirements/FRD/invite-user/requirement.md) |
| Acceptance Criterion | [AC-4, AC-6](../../../docs/02-requirements/FRD/invite-user/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-008](TESTPLAN-TEN-008.md) |
| Priority | P0 |
| Type | Functional (manual, needs SMTP and Keycloak) |
| Automated | No |

## Steps
1. With real SMTP and Keycloak, invite an unused address; open the email link.
2. Create the account from the page; sign out; sign in; check the membership.
3. Invite an existing account; open the link signed out; sign in from the page and come back to accept.

## Expected Result
The email arrives with organization, inviter, role and expiry; the new person can sign in with the chosen password; the login returns to the invitation page.

## Status
Not run (no SMTP or Keycloak in the build environment; tests use stand-ins for both).
