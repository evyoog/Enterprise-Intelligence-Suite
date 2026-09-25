# TC-TEN-004: Organization Lifecycle — AC-4

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-004 |
| Requirement ID (required) | [REQ-TEN-001](../../../docs/02-requirements/FRD/organization-lifecycle/requirement.md) |
| Acceptance Criterion | [AC-4](../../../docs/02-requirements/FRD/organization-lifecycle/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-001](TESTPLAN-TEN-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An organization.

## Steps
1. Arrange: an organization.
2. Act: the administrator closes it.
3. Observe the response and the UI.

## Expected Result
Its lifecycle is CLOSED, member logins are disabled, no row is deleted, it cannot be edited or suspended, and it can be activated again.

## Automated coverage
- `backend/…/AdminOrganizationLifecycleTest.java` — "closeIsSoftAndCanBeReversed", "aClosedOrganizationCannotBeSuspendedOrEdited"
- `frontend/src/pages/admin/RegistrationsAdminPage.test.tsx` — "offers Activate and hides Close for a closed organization"

## Actual Result
The automated tests below passed on 2026-09-25.

## Status
Passed (automated run 2026-09-25)

## Linked Defect (if failed)
None.
