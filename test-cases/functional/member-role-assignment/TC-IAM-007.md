# TC-IAM-007: Member Role Assignment — AC-1

| Field | Value |
|---|---|
| Test Case ID (required) | TC-IAM-007 |
| Requirement ID (required) | [REQ-IAM-002](../../../docs/02-requirements/FRD/member-role-assignment/requirement.md) |
| Acceptance Criterion | [AC-1](../../../docs/02-requirements/FRD/member-role-assignment/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-IAM-002](TESTPLAN-IAM-002.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A caller with `MANAGE_USERS`.

## Steps
1. Arrange: a caller with `MANAGE_USERS`.
2. Act: they open the member list.
3. Observe the response and the UI.

## Expected Result
Every member of their organization is shown with name, email, role and status.

## Automated coverage
- `frontend/src/components/organization/OrganizationMembersCard.test.tsx` — "lists members and changes a role"

## Actual Result
All automated tests below passed on 2026-09-25.

## Status
Passed (automated run 2026-09-25)

## Linked Defect (if failed)
None.
