# TC-TEN-003: Organization Lifecycle — AC-3

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-003 |
| Requirement ID (required) | [REQ-TEN-001](../../../docs/02-requirements/FRD/organization-lifecycle/requirement.md) |
| Acceptance Criterion | [AC-3](../../../docs/02-requirements/FRD/organization-lifecycle/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-001](TESTPLAN-TEN-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A SUSPENDED or CLOSED organization.

## Steps
1. Arrange: a SUSPENDED or CLOSED organization.
2. Act: the administrator activates it.
3. Observe the response and the UI.

## Expected Result
Its lifecycle is ACTIVE and every active member's login is enabled.

## Automated coverage
- `backend/…/AdminOrganizationLifecycleTest.java` — "suspendDisablesEveryActiveMemberAndEndsTheirSessionsAndActivateReEnablesThem", "closeIsSoftAndCanBeReversed"
- `frontend/src/pages/admin/RegistrationsAdminPage.test.tsx` — "offers Activate and hides Close for a closed organization"

## Actual Result
The automated tests below passed on 2026-09-25.

## Status
Passed (automated run 2026-09-25)

## Linked Defect (if failed)
None.
