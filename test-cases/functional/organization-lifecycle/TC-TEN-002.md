# TC-TEN-002: Organization Lifecycle — AC-2

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-002 |
| Requirement ID (required) | [REQ-TEN-001](../../../docs/02-requirements/FRD/organization-lifecycle/requirement.md) |
| Acceptance Criterion | [AC-2](../../../docs/02-requirements/FRD/organization-lifecycle/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-001](TESTPLAN-TEN-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An ACTIVE organization with active members.

## Steps
1. Arrange: an ACTIVE organization with active members.
2. Act: the administrator suspends it with a reason.
3. Observe the response and the UI.

## Expected Result
Its lifecycle is SUSPENDED, every active member's login is disabled and their sessions ended, the page reports the count, and an `ORGANIZATION_SUSPENDED` audit record holds the reason.

## Automated coverage
- `backend/…/AdminOrganizationLifecycleTest.java` — "suspendDisablesEveryActiveMemberAndEndsTheirSessionsAndActivateReEnablesThem"
- `frontend/src/pages/admin/RegistrationsAdminPage.test.tsx` — "suspends with a reason and reports how many logins were disabled"

## Actual Result
The automated tests below passed on 2026-09-25.

## Status
Passed (automated run 2026-09-25)

## Linked Defect (if failed)
None.
