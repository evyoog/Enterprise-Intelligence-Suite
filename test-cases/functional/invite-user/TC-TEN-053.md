# TC-TEN-053: Invite user — structure node and product access

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-053 |
| Requirement ID (required) | [REQ-TEN-008](../../../docs/02-requirements/FRD/invite-user/requirement.md) |
| Acceptance Criterion | [AC-10](../../../docs/02-requirements/FRD/invite-user/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-008](TESTPLAN-TEN-008.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Steps
1. Invite with a structure node and accept.
2. Use a node of another organization.

## Expected Result
The node is applied on acceptance; a foreign node is refused; no permission or product access comes from it.

## Automated coverage
- `backend/.../invitation/service/InvitationServiceTest.java` — `theStructureNodeIsOptionalActiveAndAppliedOnAcceptanceAndGrantsNothing`

## Actual Result
The automated tests passed on 2026-10-08.

## Status
Passed
