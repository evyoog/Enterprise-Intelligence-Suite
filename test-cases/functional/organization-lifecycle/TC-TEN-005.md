# TC-TEN-005: Organization Lifecycle — AC-5

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-005 |
| Requirement ID (required) | [REQ-TEN-001](../../../docs/02-requirements/FRD/organization-lifecycle/requirement.md) |
| Acceptance Criterion | [AC-5](../../../docs/02-requirements/FRD/organization-lifecycle/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-001](TESTPLAN-TEN-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
Keycloak fails to update a member's login.

## Steps
1. Arrange: keycloak fails to update a member's login.
2. Act: a lifecycle action runs.
3. Observe the response and the UI.

## Expected Result
The status change stands, the page lists that member's email with a retry hint, and repeating the action retries.

## Automated coverage
- `backend/…/AdminOrganizationLifecycleTest.java` — "aKeycloakFailureIsReportedAndTheActionCanBeRepeatedToRetry"
- `frontend/src/pages/admin/RegistrationsAdminPage.test.tsx` — "warns about logins Keycloak did not update"

## Actual Result
The automated tests below passed on 2026-09-25.

## Status
Passed (automated run 2026-09-25)

## Linked Defect (if failed)
None.
