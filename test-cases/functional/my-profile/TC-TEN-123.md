# TC-TEN-123: My profile — names and external identity providers (nameEditable)

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-123 |
| Requirement ID (required) | [REQ-TEN-011](../../../docs/02-requirements/FRD/my-profile/requirement.md) (BR-PRF-016) |
| Acceptance Criterion | [AC-12](../../../docs/02-requirements/FRD/my-profile/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-011](TESTPLAN-TEN-011.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A person who signed in with SAML or OIDC in this version; the profile screen tested with a stubbed `account.nameEditable` false.

## Steps
1. For the person who signed in through SAML or OIDC read `account.nameEditable`, change the names, then sign in again through SSO and read the names.
2. Open the screen with a profile answer whose `account.nameEditable` is false.

## Expected Result
`nameEditable` is true for every person in this version: the SSO person can change the names and a later SSO sign-in does not overwrite them (an account is given names only when it is first created). With a stubbed false value the screen shows first and last name read-only with an explanation and does not send them as editable.

## Automated coverage
- `frontend/src/pages/ProfilePage.test.tsx` — `shows the names as managed by the identity provider when they are not editable`
- `frontend/src/utils/profileValidation.test.ts` — `skips the two names when they are not editable (managed by an identity provider)`

## Actual Result
Not run yet.

## Status
Not Run
