# TC-IAM-033: Privileged Access (User and Organization Administrator) — AC-11

| Field | Value |
|---|---|
| Test Case ID (required) | TC-IAM-033 |
| Requirement ID (required) | [REQ-IAM-004](../../../docs/02-requirements/FRD/privileged-access/requirement.md) |
| Acceptance Criterion | [AC-11](../../../docs/02-requirements/FRD/privileged-access/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-IAM-004](TESTPLAN-IAM-004.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A user who is not an active organization member.

## Steps
1. Arrange: a user who is not an active organization member.
2. Act: they open the request form.
3. Observe the response and the UI.

## Expected Result
No ORGANIZATION-scope permission is listed.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/authorization/service/PrivilegedAccessServiceTest.java` — "organizationScopePermissionsAreListedOnlyForOrganizationMembers"

## Actual Result
All automated tests below passed on 2026-09-25.

## Status
Passed (automated run 2026-09-25)

## Linked Defect (if failed)
None.
