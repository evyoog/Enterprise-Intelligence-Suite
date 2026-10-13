# TC-TEN-058: Invite user — the inviter is told when the invitation is declined

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-058 |
| Requirement ID (required) | [REQ-TEN-008](../../../docs/02-requirements/FRD/invite-user/requirement.md) (REQ-TEN-008.19, BR-INV-018) |
| Acceptance Criterion | [AC-13](../../../docs/02-requirements/FRD/invite-user/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-008](TESTPLAN-TEN-008.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes (`InvitationServiceTest.theInviterIsNotifiedWhenTheInvitationIsDeclinedAndOnlyTheInviter`) |

## Steps
1. An administrator invites an address; a second administrator exists.
2. The person declines from the link.
3. Open both administrators' notification centers.

## Expected Result
Only the inviter has the notification "Invitation declined" naming the email. The other administrator has none.

## Status
Passed (automated, 2026-10-12).
