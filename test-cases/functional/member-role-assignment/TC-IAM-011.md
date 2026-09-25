# TC-IAM-011: Member Role Assignment — AC-5

| Field | Value |
|---|---|
| Test Case ID (required) | TC-IAM-011 |
| Requirement ID (required) | [REQ-IAM-002](../../../docs/02-requirements/FRD/member-role-assignment/requirement.md) |
| Acceptance Criterion | [AC-5](../../../docs/02-requirements/FRD/member-role-assignment/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-IAM-002](TESTPLAN-IAM-002.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A caller without `MANAGE_USERS`.

## Steps
1. Arrange: a caller without `MANAGE_USERS`.
2. Act: they open the member list.
3. Observe the response and the UI.

## Expected Result
The response is 403 and the UI handles it like `BusinessDashboardPage`.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/registration/service/OrganizationSelfServiceTest.java` — "regularMemberCannotChangeAnyonesRole"
- `frontend/src/components/organization/OrganizationMembersCard.test.tsx` — "renders nothing without MANAGE_USERS"

## Actual Result
All automated tests below passed on 2026-09-25.

## Status
Passed (automated run 2026-09-25)

## Linked Defect (if failed)
None.
