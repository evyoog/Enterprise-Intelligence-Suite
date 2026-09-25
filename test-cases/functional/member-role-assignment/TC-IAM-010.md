# TC-IAM-010: Member Role Assignment — AC-4

| Field | Value |
|---|---|
| Test Case ID (required) | TC-IAM-010 |
| Requirement ID (required) | [REQ-IAM-002](../../../docs/02-requirements/FRD/member-role-assignment/requirement.md) |
| Acceptance Criterion | [AC-4](../../../docs/02-requirements/FRD/member-role-assignment/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-IAM-002](TESTPLAN-IAM-002.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A member id from another organization.

## Steps
1. Arrange: a member id from another organization.
2. Act: the caller changes its role.
3. Observe the response and the UI.

## Expected Result
The response is 403 "You do not have permission to do this".

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/registration/service/OrganizationSelfServiceTest.java` — "cannotChangeARoleForAMemberOfAnotherOrganization"

## Actual Result
All automated tests below passed on 2026-09-25.

## Status
Passed (automated run 2026-09-25)

## Linked Defect (if failed)
None.
