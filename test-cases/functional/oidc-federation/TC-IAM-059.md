# TC-IAM-059: Oidc Federation — AC-5

| Field | Value |
|---|---|
| Test Case ID (required) | TC-IAM-059 |
| Requirement ID (required) | [REQ-IAM-006](../../../docs/02-requirements/FRD/oidc-federation/requirement.md) |
| Acceptance Criterion | [AC-5](../../../docs/02-requirements/FRD/oidc-federation/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-IAM-006](TESTPLAN-IAM-006.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Partly |

## Preconditions
An enabled OIDC provider.

## Steps
1. Arrange: an enabled OIDC provider.
2. Act: a member enters the organization code and signs in at the provider.
3. Observe the response and the UI.

## Expected Result
They are provisioned as MEMBER and get a session; signing in again with the same subject maps to the same account.

## Automated coverage
- `backend/…/federation/service/OidcFederationTest.java` — "aValidSignInProvisionsAMemberAndFinalizesTheSessionAndTheSameSubjectMapsBackToTheSameCustomer"
- `frontend/src/components/home/AuthModalOidc.test.tsx` — "navigates to the OIDC login-init"

**Manual check:** Sign in end to end against a real test tenant (for example Azure AD or Okta) in the dev environment; the automated tests use an in-memory provider.

## Actual Result
The automated tests below passed on 2026-09-26; the manual check is not yet run.

## Status
Not Run

## Linked Defect (if failed)
None.
