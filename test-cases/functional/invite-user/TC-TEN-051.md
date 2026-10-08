# TC-TEN-051: Invite user — existing members and one organization

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-051 |
| Requirement ID (required) | [REQ-TEN-008](../../../docs/02-requirements/FRD/invite-user/requirement.md) |
| Acceptance Criterion | [AC-8](../../../docs/02-requirements/FRD/invite-user/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-008](TESTPLAN-TEN-008.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Steps
1. Invite an active, a suspended and a removed member.
2. Accept as a person active in another organization.

## Expected Result
Active and suspended members are refused; a removed member returns on the same row with the new role; a person active elsewhere is refused on acceptance and not moved.

## Automated coverage
- `backend/.../invitation/service/InvitationServiceTest.java` — `activeAndSuspendedMembersAreRefusedAndPendingInvitationsHoldNoSeat`, `aPersonActiveInAnotherOrganizationIsRefusedAndNotMoved`, `aRemovedMemberIsReadmittedOnTheSameRowWithTheNewRole`

## Actual Result
The automated tests passed on 2026-10-08.

## Status
Passed
