# TC-TEN-052: Invite user — organization state

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-052 |
| Requirement ID (required) | [REQ-TEN-008](../../../docs/02-requirements/FRD/invite-user/requirement.md) |
| Acceptance Criterion | [AC-9](../../../docs/02-requirements/FRD/invite-user/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-008](TESTPLAN-TEN-008.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Steps
1. Suspend the organization.
2. Invite and accept.

## Expected Result
Both are refused with "This organization is currently unavailable for new members."; the public page shows the organization as unavailable.

## Automated coverage
- `backend/.../invitation/service/InvitationServiceTest.java` — `aSuspendedOrClosedOrganizationCannotInviteOrAccept`

## Actual Result
The automated tests passed on 2026-10-08.

## Status
Passed
