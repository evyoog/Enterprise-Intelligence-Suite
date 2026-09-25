# TC-TEN-007: Organization Lifecycle — AC-7

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-007 |
| Requirement ID (required) | [REQ-TEN-001](../../../docs/02-requirements/FRD/organization-lifecycle/requirement.md) |
| Acceptance Criterion | [AC-7](../../../docs/02-requirements/FRD/organization-lifecycle/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-001](TESTPLAN-TEN-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An organization whose registration is pending email verification.

## Steps
1. Arrange: an organization whose registration is pending email verification.
2. Act: it is activated.
3. Observe the response and the UI.

## Expected Result
No login is enabled.

## Automated coverage
- `backend/…/AdminOrganizationLifecycleTest.java` — "activatingDoesNotEnableAnAdminStillAwaitingEmailVerification"

## Actual Result
The automated tests below passed on 2026-09-25.

## Status
Passed (automated run 2026-09-25)

## Linked Defect (if failed)
None.
