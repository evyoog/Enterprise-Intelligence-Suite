# TC-TEN-011: Member Lifecycle & Access Review — AC-1

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-011 |
| Requirement ID (required) | [REQ-TEN-002](../../../docs/02-requirements/FRD/member-lifecycle/requirement.md) |
| Acceptance Criterion | [AC-1](../../../docs/02-requirements/FRD/member-lifecycle/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-002](TESTPLAN-TEN-002.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An active member of an organization.

## Steps
1. Arrange: an org with an admin and a member.
2. Act: the admin suspends the member.
3. Observe the member's status and access.

## Expected Result
The member's status is SUSPENDED and `getMyOrganization` (and every self-service call) now fails as if they were not a member.

## Automated coverage
- `backend/.../registration/service/OrganizationSelfServiceTest.java` — `orgAdminCanSuspendThenReactivateATeammate`
- `backend/.../registration/service/OrganizationSelfServiceTest.java` — `suspendedMemberLosesSelfServiceImmediately`

## Actual Result
The automated tests below passed on 2026-09-26.

## Status
Passed (automated run 2026-09-26)

## Linked Defect (if failed)
None.
