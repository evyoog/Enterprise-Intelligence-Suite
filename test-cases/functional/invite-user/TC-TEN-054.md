# TC-TEN-054: Invite user — invitations screens and lifecycle

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-054 |
| Requirement ID (required) | [REQ-TEN-008](../../../docs/02-requirements/FRD/invite-user/requirement.md) |
| Acceptance Criterion | [AC-11](../../../docs/02-requirements/FRD/invite-user/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-008](TESTPLAN-TEN-008.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Steps
1. Open People & structure → Invitations; filter, invite, resend, revoke, open details.
2. Revoke, expire and decline; try to accept each.
3. As platform administrator open the organization's Members tab.

## Expected Result
Actions per status are right; accept fails with the matching message; the platform administrator sees a read-only list; a member allowed only to invite sees only the Invitations tab.

## Automated coverage
- `backend/.../invitation/service/InvitationServiceTest.java` — `revokedExpiredAndDeclinedInvitationsCannotBeAcceptedAndCanBeResent`, `anAdministratorCanReadTheOrganizationsInvitationsReadOnly`
- `frontend/src/components/organization/OrganizationInvitationsCard.test.tsx`, `frontend/src/pages/OrganizationMembersPage.test.tsx`, `frontend/src/pages/admin/AdminOrganizationDetailPage.test.tsx`, `frontend/src/components/layout/appNavigation.test.ts`

## Actual Result
The automated tests passed on 2026-10-08.

## Status
Passed
