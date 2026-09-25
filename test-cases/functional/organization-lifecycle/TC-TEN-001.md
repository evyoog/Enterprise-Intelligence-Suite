# TC-TEN-001: Organization Lifecycle — AC-1

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-001 |
| Requirement ID (required) | [REQ-TEN-001](../../../docs/02-requirements/FRD/organization-lifecycle/requirement.md) |
| Acceptance Criterion | [AC-1](../../../docs/02-requirements/FRD/organization-lifecycle/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-001](TESTPLAN-TEN-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An ACTIVE organization.

## Steps
1. Arrange: an ACTIVE organization.
2. Act: the administrator edits its details.
3. Observe the response and the UI.

## Expected Result
The new details are saved, the code, seats and statuses are unchanged, blank optional fields are cleared, and an `ORGANIZATION_UPDATED` audit record exists.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/registration/service/AdminOrganizationLifecycleTest.java` — "updateReplacesTheEditableDetailsAndDropsBillingWhenSameAsAddress"
- `frontend/src/pages/admin/RegistrationsAdminPage.test.tsx` — "edits the company details and leaves the code alone"
- `frontend/src/pages/admin/RegistrationsAdminPage.test.tsx` — "disables Save while a required field is empty"

## Actual Result
The automated tests below passed on 2026-09-25.

## Status
Passed (automated run 2026-09-25)

## Linked Defect (if failed)
None.
