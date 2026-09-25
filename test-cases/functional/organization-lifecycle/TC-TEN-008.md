# TC-TEN-008: Organization Lifecycle — AC-8

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-008 |
| Requirement ID (required) | [REQ-TEN-001](../../../docs/02-requirements/FRD/organization-lifecycle/requirement.md) |
| Acceptance Criterion | [AC-8](../../../docs/02-requirements/FRD/organization-lifecycle/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-001](TESTPLAN-TEN-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A SUSPENDED organization.

## Steps
1. Arrange: a SUSPENDED organization.
2. Act: a member calls an organization self-service endpoint with a still-valid token.
3. Observe the response and the UI.

## Expected Result
The response is 403 "Your organization's account is not currently active", and it works again after activation.

## Automated coverage
- `backend/…/AdminOrganizationLifecycleTest.java` — "membersOfASuspendedOrganizationAreRefusedSelfServiceUntilItIsActivated"

## Actual Result
The automated tests below passed on 2026-09-25.

## Status
Passed (automated run 2026-09-25)

## Linked Defect (if failed)
None.
