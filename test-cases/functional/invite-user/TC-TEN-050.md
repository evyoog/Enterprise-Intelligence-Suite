# TC-TEN-050: Invite user — seats

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-050 |
| Requirement ID (required) | [REQ-TEN-008](../../../docs/02-requirements/FRD/invite-user/requirement.md) |
| Acceptance Criterion | [AC-7](../../../docs/02-requirements/FRD/invite-user/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-008](TESTPLAN-TEN-008.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Steps
1. Fill the organization to one free seat, send two invitations.
2. Take the last seat, send a third, then accept an earlier one.

## Expected Result
Pending invitations take no seat; sending with no capacity is refused; acceptance is refused with "There are no available seats in this organization.", nothing is activated and a FAILURE audit entry is kept.

## Automated coverage
- `backend/.../invitation/service/InvitationServiceTest.java` — `activeAndSuspendedMembersAreRefusedAndPendingInvitationsHoldNoSeat`, `acceptanceChecksSeatsAgainAndRefusesWithoutActivatingAnything`

## Actual Result
The automated tests passed on 2026-10-08.

## Status
Passed
