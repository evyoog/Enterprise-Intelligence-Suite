# TC-IAM-026: Privileged Access (User and Organization Administrator) — AC-4

| Field | Value |
|---|---|
| Test Case ID (required) | TC-IAM-026 |
| Requirement ID (required) | [REQ-IAM-004](../../../docs/02-requirements/FRD/privileged-access/requirement.md) |
| Acceptance Criterion | [AC-4](../../../docs/02-requirements/FRD/privileged-access/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-IAM-004](TESTPLAN-IAM-004.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Partly |

## Preconditions
An organization administrator.

## Steps
1. Arrange: an organization administrator.
2. Act: they open approvals.
3. Observe the response and the UI.

## Expected Result
Only PENDING ORGANIZATION-scope requests of their own organization are listed.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/authorization/service/PrivilegedAccessServiceTest.java` — "anotherOrganizationsAdminCannotApproveOrReachThisRequest"
- `frontend/src/components/organization/OrganizationPrivilegedAccessCard.test.tsx` — "approves a pending request with a note and reloads"

**Manual check:** Check the approvals list contains only PENDING requests of the administrator's own organization.

## Actual Result
The automated tests below passed on 2026-09-25; the manual check is not yet run.

## Status
Not Run

## Linked Defect (if failed)
None.
