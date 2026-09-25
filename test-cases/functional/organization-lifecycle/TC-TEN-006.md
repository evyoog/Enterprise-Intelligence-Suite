# TC-TEN-006: Organization Lifecycle — AC-6

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-006 |
| Requirement ID (required) | [REQ-TEN-001](../../../docs/02-requirements/FRD/organization-lifecycle/requirement.md) |
| Acceptance Criterion | [AC-6](../../../docs/02-requirements/FRD/organization-lifecycle/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-001](TESTPLAN-TEN-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An administrator who is an active member of the organization.

## Steps
1. Arrange: an administrator who is an active member of the organization.
2. Act: they suspend or close it.
3. Observe the response and the UI.

## Expected Result
The response is 400 and the dialog shows the message; the status is unchanged.

## Automated coverage
- `backend/…/AdminOrganizationLifecycleTest.java` — "anAdminWhoIsAMemberCannotSuspendOrCloseTheirOwnOrganization"
- `frontend/src/pages/admin/RegistrationsAdminPage.test.tsx` — "shows the backend refusal and keeps the dialog open"

## Actual Result
The automated tests below passed on 2026-09-25.

## Status
Passed (automated run 2026-09-25)

## Linked Defect (if failed)
None.
