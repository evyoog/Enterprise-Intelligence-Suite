# TC-IAM-009: Member Role Assignment — AC-3

| Field | Value |
|---|---|
| Test Case ID (required) | TC-IAM-009 |
| Requirement ID (required) | [REQ-IAM-002](../../../docs/02-requirements/FRD/member-role-assignment/requirement.md) |
| Acceptance Criterion | [AC-3](../../../docs/02-requirements/FRD/member-role-assignment/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-IAM-002](TESTPLAN-IAM-002.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
The organization has exactly one active `ORG_ADMIN`.

## Steps
1. Arrange: the organization has exactly one active `ORG_ADMIN`.
2. Act: that member is changed to `MEMBER`.
3. Observe the response and the UI.

## Expected Result
The response is 400 "Cannot remove the organization's last administrator." and the UI shows that message.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/registration/service/OrganizationSelfServiceTest.java` — "cannotDemoteTheOrganizationsLastAdmin"
- `frontend/src/components/organization/OrganizationMembersCard.test.tsx` — "shows the backend refusal, for example the last administrator"

## Actual Result
All automated tests below passed on 2026-09-25.

## Status
Passed (automated run 2026-09-25)

## Linked Defect (if failed)
None.
