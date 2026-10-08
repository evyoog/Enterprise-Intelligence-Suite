# TC-TEN-046: Invite user — delegating INVITE_USERS

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-046 |
| Requirement ID (required) | [REQ-TEN-008](../../../docs/02-requirements/FRD/invite-user/requirement.md) |
| Acceptance Criterion | [AC-2, AC-3](../../../docs/02-requirements/FRD/invite-user/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-008](TESTPLAN-TEN-008.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Steps
1. An administrator turns *Can invite* on for a member.
2. The member invites (as Member only) and lists invitations.
3. The administrator turns it off.

## Expected Result
The member sends invitations, sees only their own, cannot invite an administrator and has no MANAGE_USERS; after removal new invitations are refused at once.

## Automated coverage
- `backend/.../invitation/service/InvitationServiceTest.java` — `delegationGivesInviteOnlyAsMemberAndSeesOnlyOwnInvitationsAndRemovingItStopsNewOnes`
- `frontend/src/components/organization/OrganizationMembersCard.test.tsx`

## Actual Result
The automated tests passed on 2026-10-08.

## Status
Passed
