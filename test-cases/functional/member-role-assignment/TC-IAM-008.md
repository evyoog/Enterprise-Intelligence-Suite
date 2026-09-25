# TC-IAM-008: Member Role Assignment — AC-2

| Field | Value |
|---|---|
| Test Case ID (required) | TC-IAM-008 |
| Requirement ID (required) | [REQ-IAM-002](../../../docs/02-requirements/FRD/member-role-assignment/requirement.md) |
| Acceptance Criterion | [AC-2](../../../docs/02-requirements/FRD/member-role-assignment/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-IAM-002](TESTPLAN-IAM-002.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Partly |

## Preconditions
A caller with `MANAGE_USERS`.

## Steps
1. Arrange: a caller with `MANAGE_USERS`.
2. Act: they change a `MEMBER` to `ORG_ADMIN`.
3. Observe the response and the UI.

## Expected Result
The row shows `ORG_ADMIN`, the member is notified and a `MEMBER_ROLE_CHANGED` audit record exists.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/registration/service/OrganizationSelfServiceTest.java` — "orgAdminCanPromoteATeammateToOrgAdmin"
- `frontend/src/components/organization/OrganizationMembersCard.test.tsx` — "lists members and changes a role"

**Manual check:** Check the member is notified and a `MEMBER_ROLE_CHANGED` audit record exists.

## Actual Result
The automated tests below passed on 2026-09-25; the manual check is not yet run.

## Status
Not Run

## Linked Defect (if failed)
None.
