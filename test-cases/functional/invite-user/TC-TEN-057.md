# TC-TEN-057: Invite user — the inviter is told when the invitation is accepted

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-057 |
| Requirement ID (required) | [REQ-TEN-008](../../../docs/02-requirements/FRD/invite-user/requirement.md) (REQ-TEN-008.19, BR-INV-018) |
| Acceptance Criterion | [AC-13](../../../docs/02-requirements/FRD/invite-user/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-008](TESTPLAN-TEN-008.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes (`InvitationServiceTest.theInviterIsNotifiedWhenTheInvitationIsAccepted`) |

## Steps
1. An administrator invites an existing account.
2. The invited person signs in and accepts.
3. Open the administrator's notification center.

## Expected Result
One notification "Invitation accepted" (category ORGANIZATION) names the email, the organization and the role; an email is sent unless the category is muted. The invited person gets no notification.

## Status
Passed (automated, 2026-10-12).
