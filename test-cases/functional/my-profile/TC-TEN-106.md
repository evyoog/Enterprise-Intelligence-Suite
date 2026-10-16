# TC-TEN-106: My profile — Account tab content for each role

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-106 |
| Requirement ID (required) | [REQ-TEN-011](../../../docs/02-requirements/FRD/my-profile/requirement.md) (BR-PRF-022) |
| Acceptance Criterion | [AC-6](../../../docs/02-requirements/FRD/my-profile/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-011](TESTPLAN-TEN-011.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
One person of each role; one organization administrator with an organization.

## Steps
1. Open the Account tab as a platform administrator, an organization administrator, a member and an individual.

## Expected Result
Email, role chip, organization name and code and membership status appear for organization people and are empty for an individual and a platform administrator without an organization (their member-since is the account creation date). Through the admin endpoint the role shown is the viewed person's own. Links: Security and Preferences for everyone, People & structure and the business dashboard only for an organization administrator.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/profile/service/ProfileServiceTest.java` — `theAccountSectionShowsRoleOrganizationAndMembership`
- `frontend/src/pages/ProfilePage.test.tsx` — `keeps the Account tab read-only, with links to security and preferences and the organization pages`, `does not offer the organization links to a member`

## Actual Result
Not run yet.

## Status
Not Run
